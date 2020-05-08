 /*
 Copyright (c) 2012, Intel Corporation
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 
     * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
     * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package com.intel.openAttestation.manifest.hibernate.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import com.intel.openAttestation.manifest.hibernate.domain.MLE;
 import com.intel.openAttestation.manifest.hibernate.domain.OEM;
 import com.intel.openAttestation.manifest.hibernate.domain.OS;
 import com.intel.openAttestation.manifest.hibernate.util.HibernateUtilHis;
 
 
 public class MLEDAO {
 
 	/**
 	 * Constructor to start a hibernate transaction in case one has not
 	 * already been started 
 	 */
 	public MLEDAO() {
 	}
 	
 	public MLE queryMLEidByNameAndVersionAndOEMid (String Name, String Version, String OEMname){
 		List<MLE> mleList = null;
 		try {
 			HibernateUtilHis.beginTransaction();
 			Query query = HibernateUtilHis.getSession().createQuery("select a from MLE a, OEM b where a.Name = :name and a.Version = :version and a.oem.OEMID = b.OEMID and b.Name = :oem_name");
 			//Query query = HibernateUtilHis.getSession().createQuery("select new MLE(a.Name, a.Version, a.Attestation_Type, a.MLE_Type, a.Description, b) from MLE a, OEM b where a.Name = :name and a.Version = :version and a.oem.OEMID = b.OEMID and b.Name = :oem_name");
 
 			query.setString("name", Name);
 			query.setString("version", Version);
 			query.setString("oem_name", OEMname);
 			List list = query.list();
 			mleList = (List<MLE>)list;
 			if (list.size() < 1) 
 			{
 				HibernateUtilHis.commitTransaction();
 				return null;
 			} else {
 				HibernateUtilHis.commitTransaction();
 				return (MLE)mleList.get(0);
 			}
 		} catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 		
 	}
 	
 	public MLE queryMLEidByNameAndVersionAndOSid (String Name, String Version, String OSname, String OSversion){
 		List<MLE> mleList = null;
 		try {
 			HibernateUtilHis.beginTransaction();
 			Query query = HibernateUtilHis.getSession().createQuery("select a from MLE a, OS b where a.Name = :name and a.Version = :version and a.os.ID = b.ID and b.Name = :os_name and b.Version = :os_version");
 			query.setString("name", Name);
 			query.setString("version", Version);
 			query.setString("os_name", OSname);
 			query.setString("os_version", OSversion);
 			List list = query.list();
 			mleList = (List<MLE>)list;
 			if (list.size() < 1) 
 			{
 				HibernateUtilHis.commitTransaction();
 				return null;
 			} else {
 				HibernateUtilHis.commitTransaction();
 				return (MLE)mleList.get(0);
 			}
 		} catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 		
 	}
 
     public boolean isOEMExisted(String Name){
 		boolean flag =false;
 		try {
 			HibernateUtilHis.beginTransaction();
 			Query query = HibernateUtilHis.getSession().createQuery("from OEM a where a.Name = :value");
 			query.setString("value", Name);
 			List list = query.list();
 		
 			if (list.size() < 1) {
 				flag =  false;
 			} else {
 				flag = true;
 			}
 			HibernateUtilHis.commitTransaction();
 			return flag;
 		} catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 	}
 	
     public boolean isMLEExisted(String Name,String version){
 		boolean flag =false;
 		try {
 			HibernateUtilHis.beginTransaction();
 			Query query = HibernateUtilHis.getSession().createQuery("from MLE m where m.Name = :mleName and m.Version = :mleVersion");
 			query.setString("mleName", Name);
 			query.setString("mleVersion", version);
 			List list = query.list();
 		    if (list.size() > 0 )
 		    	flag = true;
 			HibernateUtilHis.commitTransaction();
 			return flag;
 		} catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 	}
 	
 	public OEM addOEMEntry(OEM OEMEntry){
 		try {
 			HibernateUtilHis.beginTransaction();
 			HibernateUtilHis.getSession().save(OEMEntry);
 			HibernateUtilHis.commitTransaction();
 			return OEMEntry;
 		} catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 
 	}
 	
 
 	public MLE addMLEEntry(MLE MLEEntry){
 		try {
 			HibernateUtilHis.beginTransaction();
 			HibernateUtilHis.getSession().save(MLEEntry);
 			HibernateUtilHis.commitTransaction();
 			return MLEEntry;
 		} catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 
 	}
 	
 
 	public MLE getMLE(String Name,String Version){
 		try{
 		    MLE mle = null;
 		    Query query = HibernateUtilHis.getSession().createQuery("from MLE m where m.Name = :name and m.Version = :version");
 		    query.setString("name", Name);
 		    query.setString("version", Version);
 		    List list = query.list();
 		    if (list.size() >= 1) {
 		    	mle=(MLE)list.get(0);
 			} 
 		    return mle;
 		}catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 	}
 	
 	public void editMLEDesc(String mleName, String mleVersion, String description){
 		try{
 			HibernateUtilHis.beginTransaction();
 			Session session = HibernateUtilHis.getSession();
 		    Query query = HibernateUtilHis.getSession().createQuery("from MLE m where m.Name = :name and m.Version = :version");
 		    query.setString("name",mleName);
 		    query.setString("version", mleVersion);
             List list = query.list();
             if (list.size() < 1){
             	HibernateUtilHis.rollbackTransaction();
                 throw new Exception ("Object not found");
             }
             MLE mle = (MLE)list.get(0);
             mle.setDescription(description);
             session.update(mle);
             HibernateUtilHis.commitTransaction();
 		}catch (Exception e) {
             HibernateUtilHis.rollbackTransaction();
             e.printStackTrace();
             throw new RuntimeException(e);
          }finally{
             HibernateUtilHis.closeSession();
          }
 	}
 	
 	public void DeleteOEMEntry (String OEMName){
 		try {
 			HibernateUtilHis.beginTransaction();
 			Session session = HibernateUtilHis.getSession();
 			Query query = session.createQuery("from OEM a where a.Name = :NAME");
 			query.setString("NAME", OEMName);
 			List list = query.list();
 			if (list.size() < 1){
 				HibernateUtilHis.rollbackTransaction();
 				throw new Exception ("Object not found");
 			}
 			OEM OEMEntry = (OEM)list.get(0);
 			session.delete(OEMEntry);
 			HibernateUtilHis.commitTransaction();
 		} catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 		
 	}
 
 
 	public MLE DeleteMLEEntry (String name,String version){
 		try {
 			HibernateUtilHis.beginTransaction();
 			Session session = HibernateUtilHis.getSession();
 			Query query = session.createQuery("from MLE a where a.Name = :name and a.Version = :version");
 			query.setString("name", name);
 			query.setString("version",version);
 			List list = query.list();
 			if (list.size() < 1){
 				HibernateUtilHis.rollbackTransaction();
 				throw new Exception ("Object not found");
 			}
 			MLE MLEEntry = (MLE)list.get(0);
 			session.delete(MLEEntry);
 			HibernateUtilHis.commitTransaction();
 			return MLEEntry;
 		} catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 		
 	}
 
 	public boolean isMLEExisted(String name, String version, String osName,
 			String osVersion, String oemName) {
 		
 		String[] queryString = new String[2];
 		int order =0;
 		boolean flag= false;
 		try{
 			HibernateUtilHis.beginTransaction();
 			queryString[0]="select m from MLE m inner join m.oem o where m.Name=:name and m.Version=:version and o.Name =:oemName";//query oem information
 			queryString[1]="select m from MLE m inner join m.os o where m.Name=:name and m.Version=:version and o.Name=:osName and o.Version=:osVersion";//query os information
 			if (oemName!=null)
 				order = 0;
 			else if (osName!=null && osVersion !=null)
 				order =1;
 			Query query = HibernateUtilHis.getSession().createQuery(queryString[order]);
 			query.setString("name", name);
 			query.setString("version", version);
 			if (order ==0)
 				query.setString("oemName", oemName);
 			else if(order ==1){
 				query.setString("osName", osName);
 				query.setString("osVersion", osVersion);
 			}
 			
 			List list = query.list();
 			if(list.size()>0)
 				flag =true;
 			HibernateUtilHis.commitTransaction();
 			return flag;
 		} catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 	}
 	
 	public OEM queryOEMByNameAndVersionAndOEMid(String Name, String Version, String OEMname){
 		List<OEM> OEMList = null;
 		try {
 			HibernateUtilHis.beginTransaction();
 			Query query = HibernateUtilHis.getSession().createQuery("select b from MLE a, OEM b where a.Name = :name and a.Version = :version and a.oem.OEMID = b.OEMID and b.Name = :oem_name");
 			query.setString("name", Name);
 			query.setString("version", Version);
 			query.setString("oem_name", OEMname);
 			List list = query.list();
 			OEMList = (List<OEM>)list;
 			if (list.size() < 1) 
 			{
 				HibernateUtilHis.commitTransaction();
 				return null;
 			} else {
 				HibernateUtilHis.commitTransaction();
 				return (OEM)OEMList.get(0);
 			}
 		} catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 		
 	}
 	
 	public OS queryOSByNameAndVersionAndOSid (String Name, String Version, String OSname, String OSversion){
 		List<OS> OSList = null;
 		try {
 			HibernateUtilHis.beginTransaction();
 			Query query = HibernateUtilHis.getSession().createQuery("select b from MLE a, OS b where a.Name = :name and a.Version = :version and a.os.ID = b.ID and b.Name = :os_name and b.Version = :os_version");
 			query.setString("name", Name);
 			query.setString("version", Version);
 			query.setString("os_name", OSname);
 			query.setString("os_version", OSversion);
 			List list = query.list();
 			OSList = (List<OS>)list;
 			if (list.size() < 1) 
 			{
 				HibernateUtilHis.commitTransaction();
 				return null;
 			} else {
 				HibernateUtilHis.commitTransaction();
 				return (OS)OSList.get(0);
 			}
 		} catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 		
 	}
 
 	public List<MLE> getAllMLEEntries(){
 		try{
 			HibernateUtilHis.beginTransaction();
 			ArrayList<MLE> MLEList = new ArrayList<MLE>();
 			Query query = HibernateUtilHis.getSession().createQuery("from MLE mle");
 			System.out.println("query:"+query.toString());
 			List list = query.list();
 			for (int i=0;i<list.size();i++){
 				MLEList.add((MLE)list.get(i));
 			}
 			HibernateUtilHis.commitTransaction();
 			return MLEList;
 		}catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 		
 	}    	
 	
 	public OEM queryOEMByMLEID(long id){
 		List<OEM> OEMList = null;
 		try {
 			HibernateUtilHis.beginTransaction();
 			Query query = HibernateUtilHis.getSession().createQuery("select a.oem from MLE a inner join a.oem o where a.MLEID = :id");
 			query.setLong("id", id);
 			List list = query.list();
 			OEMList = (List<OEM>)list;
 			if (list.size() < 1) 
 			{
 				HibernateUtilHis.commitTransaction();
 				return null;
 			} else {
 				HibernateUtilHis.commitTransaction();
 				return (OEM)OEMList.get(0);
 			}
 		} catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 		
 	}
 
 	public OS queryOSByMLEID(long id){
 		List<OS> OSList = null;
 		try {
 			HibernateUtilHis.beginTransaction();
 			Query query = HibernateUtilHis.getSession().createQuery("select a.os from MLE a inner join a.os o where a.MLEID = :id");
 			query.setLong("id", id);
 			List list = query.list();
 			OSList = (List<OS>)list;
 			if (list.size() < 1) 
 			{
 				HibernateUtilHis.commitTransaction();
 				return null;
 			} else {
 				HibernateUtilHis.commitTransaction();
 				return (OS)OSList.get(0);
 			}
 		} catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 		
 	}
 	
 	public MLE queryMLEByCriteria(String criteria, String type, Long mleID){
 		List<MLE> mleList = null;
 		Query query;
 		try {
 			HibernateUtilHis.beginTransaction();
 	        if (type.equalsIgnoreCase("oem")){
				query = HibernateUtilHis.getSession().createQuery("select a from MLE a inner join a.oem b where (a.Name like '%"+criteria+"%' or a.Version like '%"+criteria+"%'" +
						" or a.Description like '%"+criteria+"%' or a.oem.Name like '%"+criteria+"%') and a.MLEID = :value ");
 				query.setLong("value", mleID);
 	        } else if (type.equalsIgnoreCase("os")){
				query = HibernateUtilHis.getSession().createQuery("select a from MLE a inner join a.os b where (a.Name like '%"+criteria+"%' or a.Version like '%"+criteria+"%'" +
						" or a.Description like '%"+criteria+"%' or a.os.Name like '%"+criteria+"%' or a.os.Version like '%"+criteria+"%') and a.MLEID = :value ");
 				query.setLong("value", mleID);
 	        } else {
 	        	return null;
 	        }
 			List list = query.list();
 			mleList = (List<MLE>)list;
 			if (list.size() < 1) 
 			{
 				HibernateUtilHis.commitTransaction();
 				return null;
 			} else {
 				HibernateUtilHis.commitTransaction();
 				return (MLE)mleList.get(0);
 			}
 		} catch (Exception e) {
 			HibernateUtilHis.rollbackTransaction();
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}finally{
 			HibernateUtilHis.closeSession();
 		}
 		
 	}
 }
