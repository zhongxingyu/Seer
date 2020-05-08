 package jp.dqneo.amazons3.sample;
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
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.UUID;
 
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.auth.PropertiesCredentials;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 
 /**
 * This jp.dqneo.amazons3.sample demonstrates how to make basic requests to Amazon S3 using
  * the AWS SDK for Java.
  * <p>
  * <b>Prerequisites:</b> You must have a valid Amazon Web Services developer
  * account, and be signed up to use Amazon S3. For more information on
  * Amazon S3, see http://aws.amazon.com/s3.
  * <p>
  * <b>Important:</b> Be sure to fill in your AWS access credentials in the
  *                   AwsCredentials.properties file before you try to run this
 *                   jp.dqneo.amazons3.sample.
  * http://aws.amazon.com/security-credentials
  */
 public class S3Sample {
 
     public static void main(String[] args) throws IOException {
         /*
          * Important: Be sure to fill in your AWS access credentials in the
          *            AwsCredentials.properties file before you try to run this
         *            jp.dqneo.amazons3.sample.
          * http://aws.amazon.com/security-credentials
          */
         AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
                 S3Sample.class.getResourceAsStream("AwsCredentials.properties")));
 
         s3.setEndpoint("https://s3-ap-northeast-1.amazonaws.com");
 
         String bucketName = "my-first-s3-bucket-" + UUID.randomUUID();
         String key = "2/foo/bar";
 
         System.out.println("===========================================");
         System.out.println("Getting Started with Amazon S3");
         System.out.println("===========================================\n");
 
         try {
 
             /*
              * Upload an object to your bucket - You can easily upload a file to
              * S3, or upload directly an InputStream if you know the length of
              * the data in the stream. You can also specify your own metadata
              * when uploading to S3, which allows you set a variety of options
              * like content-type and content-encoding, plus additional metadata
              * specific to your applications.
              */
             System.out.println("Uploading a new object to S3 from a file\n");
             PutObjectRequest p =new PutObjectRequest(bucketName, key, createSampleFile());
             s3.putObject(p);
             System.out.println("ok\n");
 
         } catch (AmazonServiceException ase) {
             System.out.println("Caught an AmazonServiceException, which means your request made it "
                     + "to Amazon S3, but was rejected with an error response for some reason.");
             System.out.println("Error Message:    " + ase.getMessage());
             System.out.println("HTTP Status Code: " + ase.getStatusCode());
             System.out.println("AWS Error Code:   " + ase.getErrorCode());
             System.out.println("Error Type:       " + ase.getErrorType());
             System.out.println("Request ID:       " + ase.getRequestId());
         } catch (AmazonClientException ace) {
             System.out.println("Caught an AmazonClientException, which means the client encountered "
                     + "a serious internal problem while trying to communicate with S3, "
                     + "such as not being able to access the network.");
             System.out.println("Error Message: " + ace.getMessage());
         }
     }
 
     /**
      * Creates a temporary file with text data to demonstrate uploading a file
      * to Amazon S3
      *
      * @return A newly created temporary file with text data.
      *
      * @throws IOException
      */
     private static File createSampleFile() throws IOException {
         File file = File.createTempFile("aws-java-sdk-", ".txt");
         file.deleteOnExit();
 
         Writer writer = new OutputStreamWriter(new FileOutputStream(file));
         writer.write("abcdefghijklmnopqrstuvwxyz\n");
         writer.write("01234567890112345678901234\n");
         writer.write("!@#$%^&*()-=[]{};':',.<>/?\n");
         writer.write("01234567890112345678901234\n");
         writer.write("abcdefghijklmnopqrstuvwxyz\n");
         writer.close();
 
         return file;
     }
 
 
 }
