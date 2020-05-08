 /*
 Copyright (c) 2012, Intel Corporation
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 
     * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
     * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package com.intel.openAttestation.manifest.resource;
 
 import java.util.HashMap;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import gov.niarl.hisAppraiser.util.HisUtil;
 
 import com.intel.openAttestation.manifest.bean.OpenAttestationResponseFault;
 import com.intel.openAttestation.manifest.bean.PcrWhiteListBean;
 import com.intel.openAttestation.manifest.hibernate.dao.*;
 import com.intel.openAttestation.manifest.hibernate.domain.PcrWhiteList;
 import com.intel.openAttestation.manifest.resource.PcrWhiteListResource;
 import com.intel.openAttestation.manifest.hibernate.domain.MLE;
 
 /**
  * RESTful web service interface to work with OEM DB.
  * @author xmei1
  *
  */
 
 @Path("resources/mles/whitelist/pcr")
 public class PcrWhiteListResource {
 
 	@POST
 	@Consumes("application/json")
 	@Produces("application/json")
 	public Response addPcrWhiteList(@Context UriInfo uriInfo, PcrWhiteListBean pcrbean,
 			@Context javax.servlet.http.HttpServletRequest request){
         UriBuilder b = uriInfo.getBaseUriBuilder();
         b = b.path(PcrWhiteListResource.class);
 		Response.Status status = Response.Status.OK;
 		boolean isValidKey = true;
         try{
 			PcrWhiteListDAO dao = new PcrWhiteListDAO();
 			PcrWhiteList pcr = new PcrWhiteList();
 			MLEDAO daoMLE = new MLEDAO();
 			MLE mle = null;
 			HashMap parameters = new HashMap();
 			if (pcrbean.getPcrName()!=null){
 				parameters.put(pcrbean.getPcrName(), 10);
 			} else {
 				isValidKey = false;
 			}
 			
 			if (pcrbean.getPcrDigest()!=null){
 				parameters.put(pcrbean.getPcrDigest(), 100);
 			}
 			
 			if (! isValidKey || pcrbean.getPcrName().length() < 1 || !HisUtil.validParas(parameters) || pcrbean.getPcrDigest() == null || pcrbean.getPcrDigest().length() == 0 ){
 				status = Response.Status.INTERNAL_SERVER_ERROR;
 				OpenAttestationResponseFault fault = new OpenAttestationResponseFault(
 						OpenAttestationResponseFault.FaultCode.FAULT_500);
 				if (pcrbean.getPcrDigest() == null  || pcrbean.getPcrDigest().length() == 0){
 					fault.setError_message("Valid PCR disgest required");
 				} else {
 					fault.setError_message("Add PCR entry failed, please check the length for each parameters" +
 							" and remove all of the unwanted characters belonged to [# & + : \" \']");
 				}
 				return Response.status(status).header("Location", b.build()).entity(fault).build();
 			}
 			
 			if(pcrbean.getPcrName() != null && pcrbean.getPcrDigest() != null && pcrbean.getMLEName() != null && pcrbean.getMLEVersion() != null && pcrbean.getOEMName() != null)
 			{
 				mle = daoMLE.queryMLEidByNameAndVersionAndOEMid(pcrbean.getMLEName(), pcrbean.getMLEVersion(), pcrbean.getOEMName());
 			}
 			else if(pcrbean.getPcrName() != null && pcrbean.getPcrDigest() != null && pcrbean.getMLEName() != null && pcrbean.getMLEVersion() != null && pcrbean.getOSName() != null && pcrbean.getOSVersion() != null)
 			{
 				mle = daoMLE.queryMLEidByNameAndVersionAndOSid(pcrbean.getMLEName(), pcrbean.getMLEVersion(), pcrbean.getOSName(), pcrbean.getOSVersion());
 			}
 			if(mle == null || dao.isPcrExisted(pcrbean.getPcrName(), mle.getMLEID()))
 			{
 				status = Response.Status.BAD_REQUEST;
 				OpenAttestationResponseFault fault = new OpenAttestationResponseFault(1006);
 				if(mle == null)
 					fault.setError_message("Data Error - MLE " + pcrbean.getMLEName() +" does not exist in the database");
 				else
 					fault.setError_message("Data Error - PCR " + pcrbean.getPcrName() +" exists in the database");
 				return Response.status(status).header("Location", b.build()).entity(fault)
 						.build();
 				
 			}
 			pcr.setPcrName(pcrbean.getPcrName());
 			pcr.setPcrDigest(pcrbean.getPcrDigest());
 			pcr.setMle(mle);
 			dao.addPcrEntry(pcr);
 	        return Response.status(status).header("Location", b.build()).type(MediaType.TEXT_PLAIN).entity("True")
 	        		.build();
 		}catch (Exception e){
 			status = Response.Status.INTERNAL_SERVER_ERROR;
 			OpenAttestationResponseFault fault = new OpenAttestationResponseFault(
 					OpenAttestationResponseFault.FaultCode.FAULT_500);
 			fault.setError_message("Add PCR entry failed." + "Exception:" + e.getMessage());
 			return Response.status(status).header("Location", b.build()).entity(fault)
 					.build();
 		}
 
 	}
 
 	@PUT
 	@Consumes("application/json")
 	@Produces("application/json")
 	public Response editPcrWhiteList(@Context UriInfo uriInfo, PcrWhiteListBean pcrbean,
 			@Context javax.servlet.http.HttpServletRequest request){
         UriBuilder b = uriInfo.getBaseUriBuilder();
         b = b.path(PcrWhiteListResource.class);
 		Response.Status status = Response.Status.OK;
 		boolean isValidKey = true;
         try{
         	PcrWhiteListDAO dao = new PcrWhiteListDAO();
 			PcrWhiteList pcr = null;
 			
 			HashMap parameters = new HashMap();
 			
 			if (pcrbean.getPcrName()!=null){
 				parameters.put(pcrbean.getPcrName(), 10);
 			} else {
 				isValidKey = false;
 			}
 			
 			if (pcrbean.getPcrDigest()!=null){
 				parameters.put(pcrbean.getPcrDigest(), 100);
 			}
 
			if (!isValidKey || pcrbean.getPcrName().length() < 1 || !HisUtil.validParas(parameters) || pcrbean.getPcrDigest() == null || pcrbean.getPcrDigest().length() < 1){
 				status = Response.Status.INTERNAL_SERVER_ERROR;
 				OpenAttestationResponseFault fault = new OpenAttestationResponseFault(
 						OpenAttestationResponseFault.FaultCode.FAULT_500);
 				fault.setError_message("Edit PCR entry failed, please check the length for each parameters" +
 						" and remove all of the unwanted characters belonged to [# & + : \" \']");
 				return Response.status(status).header("Location", b.build()).entity(fault).build();
 			}
 			
 			if(pcrbean.getPcrName() != null && pcrbean.getPcrDigest() != null && pcrbean.getMLEName() != null && pcrbean.getMLEVersion() != null && pcrbean.getOEMName() != null)
 			{
 				pcr = dao.queryPcrByOEMid(pcrbean.getMLEName(), pcrbean.getMLEVersion(), pcrbean.getOEMName(), pcrbean.getPcrName());
 			}
 			else if(pcrbean.getPcrName() != null && pcrbean.getPcrDigest() != null && pcrbean.getMLEName() != null && pcrbean.getMLEVersion() != null && pcrbean.getOSName() != null && pcrbean.getOSVersion() != null)
 			{
 				pcr = dao.queryPcrByOSid(pcrbean.getMLEName(), pcrbean.getMLEVersion(), pcrbean.getOSName(), pcrbean.getOSVersion(), pcrbean.getPcrName());
 			}
 			if(pcr == null)
 			{
 				status = Response.Status.BAD_REQUEST;
 				OpenAttestationResponseFault fault = new OpenAttestationResponseFault(1006);
 				fault.setError_message("Data Error - PCR combined with the specified MLE and related information does not exist in the database");
 				return Response.status(status).header("Location", b.build()).entity(fault).build();
 				
 			}
 			pcr.setPcrDigest(pcrbean.getPcrDigest());
 			dao.editPcrEntry(pcr);
 	        return Response.status(status).header("Location", b.build()).type(MediaType.TEXT_PLAIN).entity("True")
 	        		.build();
 		}catch (Exception e){
 			status = Response.Status.INTERNAL_SERVER_ERROR;
 			OpenAttestationResponseFault fault = new OpenAttestationResponseFault(
 					OpenAttestationResponseFault.FaultCode.FAULT_500);
 			fault.setError_message("Update PCR entry failed." + "Exception:" + e.getMessage());
 			return Response.status(status).header("Location", b.build()).entity(fault)
 					.build();
 		}
 
 	}
 
 	@DELETE
 	@Produces("application/json")
 	public Response delPcrWhiteList(@QueryParam("pcrName") String pcrName, @QueryParam("mleName") String mleName, 
 			@QueryParam("mleVersion") String mleVersion, @QueryParam("oemName") String oemName, 
 			@QueryParam("osName") String osName, @QueryParam("osVersion") String osVersion, @Context UriInfo uriInfo){
         UriBuilder b = uriInfo.getBaseUriBuilder();
         b = b.path(PcrWhiteListResource.class);
 		Response.Status status = Response.Status.OK;
 		boolean isValidKey = true;
 
         try{
         	PcrWhiteListDAO dao = new PcrWhiteListDAO();
 			PcrWhiteList pcr = null;
 			
 			HashMap parameters = new HashMap();
 			if (pcrName!=null){
 				parameters.put(pcrName, 10);
 			} else {
 				isValidKey = false;
 			}
 
 			if (mleName!=null){
 				parameters.put(mleName, 50);
 			}
 
 			if(mleVersion!=null){
 				parameters.put(mleVersion, 100);
 			}
 			
 			if(oemName !=null){
 				parameters.put(oemName, 50);
 			}
 
 			if (! isValidKey || pcrName.length() < 1 || !HisUtil.validParas(parameters)){
 				status = Response.Status.INTERNAL_SERVER_ERROR;
 				OpenAttestationResponseFault fault = new OpenAttestationResponseFault(
 						OpenAttestationResponseFault.FaultCode.FAULT_500);
 				fault.setError_message("Delete PCR entry failed, please check the length for each parameters" +
 						" and remove all of the unwanted characters belonged to [# & + : \" \']");
 				return Response.status(status).header("Location", b.build()).entity(fault)
 						.build();
 			}
 			
 			if(pcrName != null && mleName != null && mleVersion != null && oemName != null)
 			{
 				pcr = dao.queryPcrByOEMid(mleName, mleVersion, oemName, pcrName);
 			}			
 			else if(pcrName != null && mleName != null && mleVersion != null && osName != null && osVersion != null)
 			{
 				pcr = dao.queryPcrByOSid(mleName, mleVersion, osName, osVersion, pcrName);
 			}	
 			
 			if(pcr == null)
 			{
 				status = Response.Status.BAD_REQUEST;	
 				OpenAttestationResponseFault fault = new OpenAttestationResponseFault(1006);
 				fault.setError_message("Data Error - PCR combined with the specified MLE and related information does not exist in the database");
 				return Response.status(status).header("Location", b.build()).entity(fault).build();
 				
 			}
 			
 			dao.deletePcrEntry(pcrName, pcr.getMle().getMLEID());
 			return Response.status(status).header("Location", b.build()).type(MediaType.TEXT_PLAIN).entity("True").build();
 		}catch (Exception e){
 			status = Response.Status.INTERNAL_SERVER_ERROR;
 			OpenAttestationResponseFault fault = new OpenAttestationResponseFault(
 					OpenAttestationResponseFault.FaultCode.FAULT_500);
 			fault.setError_message("Delete PCR entry failed." + "Exception:" + e.getMessage()); 
 			return Response.status(status).entity(fault).build();
 
 		}
 	}
 	
 
 }
