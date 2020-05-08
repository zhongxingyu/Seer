 package com.onb.otp.transformer;
 
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.springframework.stereotype.Service;
 
 import com.onb.otp.datatransferobject.OtpForCreate;
 import com.onb.otp.datatransferobject.OtpListBatchForCreateBatch;
 import com.onb.otp.datatransferobject.OtpListForAssociateOtpListWithUser;
 import com.onb.otp.datatransferobject.OtpListForCreate;
 import com.onb.otp.datatransferobject.OtpListForCreateBatch;
 import com.onb.otp.datatransferobject.OtpListForLookupOtp;
 import com.onb.otp.datatransferobject.StatusForAssociateOtpListWithUser;
 import com.onb.otp.datatransferobject.StatusForLookupOtp;
 import com.onb.otp.datatransferobject.UserForAssociateOtpListWithUser;
 import com.onb.otp.datatransferobject.UserForLookupOtp;
 import com.onb.otp.domain.OneTimePassword;
 import com.onb.otp.domain.OneTimePasswordList;
 import com.onb.otp.domain.OneTimePasswordListBatch;
 import com.onb.otp.domain.Status;
 import com.onb.otp.domain.User;
 
 @Service
 public class OtpTransformer {
 	
 	public OtpListForCreate transformOtpListForCreate(OneTimePasswordList passwordList) {
 		List<OtpForCreate> otps = new ArrayList<OtpForCreate>();
 		for (OneTimePassword password : passwordList.getPasswords()) {
 			OtpForCreate otp = new OtpForCreate();
 			otp.setIndex(password.getReferenceIndex());
 			otp.setValue(password.getCode());
 			otps.add(otp);
 		}
 		OtpListForCreate otpList = new OtpListForCreate();
 		otpList.setId(passwordList.getId());
 		otpList.setSize(passwordList.getSize());
 		otpList.setExpires(passwordList.getExpires());
 		otpList.setOtps(otps);
 		return otpList;
 	}
 	
 	public OtpListBatchForCreateBatch transformOtpListBatchForCreate(OneTimePasswordListBatch batch) {
 		Set<OtpListForCreateBatch> otpsBatch = new LinkedHashSet<OtpListForCreateBatch>();
 		for(OneTimePasswordList passwordList : batch.getPasswordLists()) {
 			OtpListForCreateBatch otpBatch = new OtpListForCreateBatch();
 			otpBatch.setId(passwordList.getId());
 			otpsBatch.add(otpBatch);
 		}
 		OtpListBatchForCreateBatch otpBatchList= new OtpListBatchForCreateBatch();
 		otpBatchList.setSize(batch.getBatchSize());
 		otpBatchList.setLists(otpsBatch);
 		return otpBatchList;
 	}
 	
 	public OtpListForLookupOtp transformOtpListForLookupOtp(OneTimePasswordList passwordList) {
 		User user = passwordList.getUser();
 
 		Status status = passwordList.getStatus();
 		StatusForLookupOtp otpStatus = new StatusForLookupOtp();
 		otpStatus.setIndex(status.getReferenceIndex());
 		otpStatus.setRemaining(status.getRemaining());
 		otpStatus.setValue(status.getValue());
 		
 		if (null != user) {
 			UserForLookupOtp otpUser = new UserForLookupOtp();
 			otpUser.setUniqueID(user.getUsername());
 			otpStatus.setUser(otpUser);
 		}
 		
 		OtpListForLookupOtp otpListLookup = new OtpListForLookupOtp();
 		otpListLookup.setId(passwordList.getId());
 		otpListLookup.setStatus(otpStatus);
 		
 		return otpListLookup;
 	}
 	
 	public OtpListForAssociateOtpListWithUser transformOtpListForAssociateOtpListWithUser(OneTimePasswordList passwordList) {
 		User user = passwordList.getUser();
 		UserForAssociateOtpListWithUser otpUser = new UserForAssociateOtpListWithUser();
 		otpUser.setUniqueID(user.getUsername());
 		
		Status status = passwordList.getStatus();
 		StatusForAssociateOtpListWithUser otpStatus = new StatusForAssociateOtpListWithUser();
 		otpStatus.setIndex(status.getReferenceIndex());
 		otpStatus.setValue(status.getValue());
 		otpStatus.setUser(otpUser);
 		
 		OtpListForAssociateOtpListWithUser otpList = new OtpListForAssociateOtpListWithUser();
 		otpList.setId(passwordList.getId());
 		otpList.setStatus(otpStatus);
 		return otpList;
 	}
 }
