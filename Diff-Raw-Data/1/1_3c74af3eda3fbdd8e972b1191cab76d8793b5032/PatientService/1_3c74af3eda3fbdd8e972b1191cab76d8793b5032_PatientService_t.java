 package com.secondopinion.common.service;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import org.apache.commons.lang3.RandomStringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Component;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.PropertiesCredentials;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.ObjectMetadata;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 import com.secondopinion.common.dao.PatientDao;
 import com.secondopinion.common.dao.UserDao;
 import com.secondopinion.common.model.FileUpload;
 import com.secondopinion.common.model.Patient;
 import com.secondopinion.common.model.PatientFile;
 import com.secondopinion.common.model.PatientMedication;
 import com.secondopinion.common.model.User;
 
 @Component
 public class PatientService {
 	private static final String BUCKET_NAME = "secondopinion";
 	private static final int KEY_LENGTH = 10;
 
 	@Autowired
 	private PatientDao patientDao;
 
 	@Autowired
 	private UserDao userDao;
 
 	public void createPatient(User user, Patient patient) {
 		patientDao.insert(user, patient);
 	}
 
 	public Patient findPatient(User user) {
 		return patientDao.findByUserId(user.getUserId());
 	}
 	
 	public Patient findPatient(int patientId) {
 		return patientDao.findByPatientId(patientId);
 	}
 	
 	public Patient getCurrentPatient() {
 		org.springframework.security.core.userdetails.User loggedInUser = (org.springframework.security.core.userdetails.User) SecurityContextHolder
 				.getContext().getAuthentication().getPrincipal();
 		String name = loggedInUser.getUsername();
 		User user = userDao.findByUserName(name);
 		return findPatient(user);
 	}
 
 	public void updatePatient(Patient patient) {
 		patientDao.updatePatient(patient);
 	}
 
 	public List<PatientMedication> getPatientMedations(Patient patient) {
 		return patientDao.fetchPatientMedications(patient.getPatientId());
 	}
 
 	public void addPatientMedation(Patient patient,
 			PatientMedication patientMedication) {
 		patientDao.insertPatientMedication(patient, patientMedication);
 	}
 
 	public void addPatientFile(Patient patient, FileUpload fileUpload)
 			throws IOException {
 		String keyName = "patient-documents/"
 				+ RandomStringUtils.randomAlphanumeric(KEY_LENGTH);
		keyName += "/" + fileUpload.fileName;
 		AWSCredentials credentials = new PropertiesCredentials(
 				PatientService.class
 						.getResourceAsStream("/AwsCredentials.properties"));
 		System.out.println(credentials);
 		AmazonS3 s3client = new AmazonS3Client(credentials);
 		try {
 			if (fileUpload.file.getSize() > 0) {
 				InputStream in = fileUpload.file.getInputStream();
 				Long contentLength = Long.valueOf(fileUpload.file.getSize());
 				ObjectMetadata metadata = new ObjectMetadata();
 				metadata.setContentLength(contentLength);
 				s3client.putObject(new PutObjectRequest(BUCKET_NAME, keyName,
 						in, metadata));
 				System.out.println("File Uploaded to S3");
 				String url = "https://s3-us-west-2.amazonaws.com/secondopinion/patient-documents/"
 						+ keyName + "/" + fileUpload.fileName;
 				PatientFile patientFile = new PatientFile(
 						patient.getPatientId(), fileUpload.fileName,
 						fileUpload.fileDescription, url);
 				patientDao.insertPatientFile(patient, patientFile);
 			}
 
 		} catch (Exception e) {
 			System.err.println("S3 Upload Error" + e.getMessage());
 		}
 	}
 
 	public void removePatientMedication(int medicationId) {
 		patientDao.deletePatientMedication(medicationId);
 	}
 }
