 /*
  * Copyright 2010-2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License").
  * You may not use this file except in compliance with the License.
  * A copy of the License is located at
  *
  *  http://aws.amazon.com/apache2.0
  *
  * or in the "license" file accompanying this file. This file is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language governing
  * permissions and limitations under the License.
  */
 package getting_started;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.auth.PropertiesCredentials;
 import com.amazonaws.services.ec2.AmazonEC2;
 import com.amazonaws.services.ec2.AmazonEC2Client;
 import com.amazonaws.services.ec2.model.CancelSpotInstanceRequestsRequest;
 import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
 import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
 import com.amazonaws.services.ec2.model.DescribeImagesResult;
 import com.amazonaws.services.ec2.model.Image;
 import com.amazonaws.services.ec2.model.LaunchSpecification;
 import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
 import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
 import com.amazonaws.services.ec2.model.SpotInstanceRequest;
 import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
 import com.amazonaws.AmazonServiceException;
 
 /**
  * Welcome to your new AWS Java SDK based project!
  *
  * This class is meant as a starting point for your console-based application that
  * makes one or more calls to the AWS services supported by the Java SDK, such as EC2,
  * SimpleDB, and S3.
  *
  * In order to use the services in this sample, you need:
  *
  *  - A valid Amazon Web Services account. You can register for AWS at:
  *       https://aws-portal.amazon.com/gp/aws/developer/registration/index.html
  *
  *  - Your account's Access Key ID and Secret Access Key:
  *       http://aws.amazon.com/security-credentials
  *
  *  - A subscription to Amazon EC2. You can sign up for EC2 at:
  *       http://aws.amazon.com/ec2/
  *
  */
 public class GettingStartedApp {
     private static final long SLEEP_CYCLE = 60000;
 
     /*
      * Important: Be sure to fill in your AWS access credentials in the
      *            AwsCredentials.properties file before you try to run this
      *            sample.
      * http://aws.amazon.com/security-credentials
      */
 
     public static void main(String[] args) throws Exception {
 
         System.out.println("===========================================");
         System.out.println("Welcome to the AWS Java SDK!");
         System.out.println("===========================================");
 
 
         /*
          * Amazon EC2
          *
          * The AWS EC2 client allows you to create, delete, and administer
          * instances programmatically.
          *
          * In this sample, we use an EC2 client to submit a Spot request,
          * wait for it to reach the active state, and then cancel and terminate
          * the associated instance.
          */
         try {
        	String API_PUBLIC_KEY="";
		String API_SECRET_KEY="";
		AWSCredentials credentials = new BasicAWSCredentials(API_PUBLIC_KEY,API_SECRET_KEY);
 
        	AmazonEC2Client ec2 = new AmazonEC2Client(credentials);
        	ec2.setEndpoint("api.greenqloud.com");	
 
        	DescribeImagesResult imgs = ec2.describeImages();
		List<Image> images = imgs.getImages();
        	for(Image image:images){
			System.out.println(image.toString());
		}	
         } catch (AmazonServiceException ase) {
     		// Write out any exceptions that may have occurred.
       		System.out.println("Caught Exception: " + ase.getMessage());
       		System.out.println("Reponse Status Code: " + ase.getStatusCode());
            	System.out.println("Error Code: " + ase.getErrorCode());
            	System.out.println("Request ID: " + ase.getRequestId());
         }
     }
 }
