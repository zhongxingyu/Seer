 package com.mac.riakcswebclient.webapp.controller;
 
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.Bucket;
 import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.List;
 
 @Controller
 @RequestMapping(value = "bucket")
 public class BucketController {
 
     private static final Logger log = LoggerFactory.getLogger(BucketController.class);
 
     @Autowired
     private AmazonS3Client amazonS3Client;
 
     /**
      * List all bucket.
      */
     @RequestMapping(value = "")
     public String listBucket(Model model) {
         List<Bucket> bucketList = amazonS3Client.listBuckets();
         model.addAttribute("bucketList", bucketList);
         model.addAttribute("pageContent", "bucket/list_bucket");
         return "layout";
     }
 
     /**
      * List all object in bucket.
      */
     @RequestMapping(value = "info/{bucketName}")
     public String listObject(@PathVariable String bucketName, Model model) {
         ObjectListing objectListing = amazonS3Client.listObjects(bucketName);
        model.addAttribute("bucketName", bucketName);
         model.addAttribute("objectSummaries", objectListing.getObjectSummaries());
         model.addAttribute("pageContent", "bucket/list_object");
         return "layout";
     }
 
     @RequestMapping(value = "create")
     public String create(Model model, @ModelAttribute Bucket bucket, HttpServletRequest request) {
         if(RequestMethod.POST.toString().equalsIgnoreCase(request.getMethod())) {
             if(!"".equals(bucket.getName())) {
                 amazonS3Client.createBucket(bucket.getName());
                 return "redirect:/bucket";
             }
         }
         model.addAttribute("pageContent", "bucket/create");
         return "layout";
     }
 
     @RequestMapping(value = "delete/{bucketName}")
     public String delete(@PathVariable String bucketName) {
         log.info("Deleting bucket {}", bucketName);
        // Delete all object in bucket.
        ObjectListing objectListing = amazonS3Client.listObjects(bucketName);
        for(S3ObjectSummary s3ObjectSummary : objectListing.getObjectSummaries()) {
            log.info("Deleting key {}", s3ObjectSummary.getKey());
            amazonS3Client.deleteObject(bucketName, s3ObjectSummary.getKey());
        }
        // Delete bucket.
         amazonS3Client.deleteBucket(bucketName);
         return "redirect:/bucket";
     }
 }
