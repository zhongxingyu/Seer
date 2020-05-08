 package org.irmacard.web.restapi.resources.surfnetCoupons;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.irmacard.credentials.Attributes;
 import org.irmacard.credentials.info.DescriptionStore;
 import org.irmacard.credentials.info.InfoException;
 import org.irmacard.credentials.info.VerificationDescription;
 import org.irmacard.web.restapi.resources.VerificationBaseResource;
 import org.irmacard.web.restapi.util.ProtocolStep;
 
 public class SurfnetVoucherVerificationResource extends
 		VerificationBaseResource {
 	final static String VERIFIER = "Surfnet";
 	final static String VERIFICATIONID = "studentVoucher";
 
 	public static final String AGE_STORE_NAME = "IRMATube.Age.Store";
 	public static final int NO_AGE_VERIFIED = 0;
 	VerificationDescription rootDescription;
 
 	public SurfnetVoucherVerificationResource() {
 		try {
 			rootDescription = DescriptionStore.getInstance()
 					.getVerificationDescriptionByName(VERIFIER, VERIFICATIONID);
 		} catch (InfoException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public ProtocolStep onSuccess(String id, Map<String, Attributes> attrMap) {
 		ProtocolStep ps = new ProtocolStep();
 		Attributes studentCard = attrMap.get(VERIFICATIONID);
 
 		if (studentCard == null) {
 			return ProtocolStep.newError(rootDescription.getName()
 					+ " credential is invalid/expired");
 		}
 
 		String studentID = new String(studentCard.get("studentID"));
 		String university = new String(studentCard.get("university"));
 		String level = new String(studentCard.get("level"));
 		String userID = studentID + "@" + university;
 		if (!level.equals("master")) {
 			return ProtocolStep.newError("Only students are eligible for a voucher");
 		}
 		
 		// Verification successful
 		String voucher = "";
 		try {
 			voucher = VoucherStore.getStore().getVoucherCode(
 					userID);
 		} catch (VoucherException e) {
 			e.printStackTrace();
 			ps.protocolDone = true;
 			ps.status = "error";
 			ps.feedbackMessage = e.getMessage();
 			return ps;
 		}
 		
 		if(voucher == null) {
 			ps.protocolDone = true;
 			ps.status = "error";
			ps.feedbackMessage = "We seem to have run out of vouchers for now. More should become available soon.";
 			return ps;
 		} else {
 			ps.protocolDone = true;
 			ps.status = "success";
 			ps.result = voucher;
 			return ps;
 		}
 	}
 
 	@Override
 	public List<VerificationDescription> getVerifications(String id) {
 		List<VerificationDescription> result = new ArrayList<VerificationDescription>();
 		result.add(rootDescription);
 		
 		return result;
 	}
 }
