 package com.orgsync.api.examples;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import com.orgsync.api.ApiClient;
 import com.orgsync.api.ApiResponse;
 import com.orgsync.api.OrgSync;
 import com.orgsync.api.OrgsResource;
 import com.orgsync.api.Resources;
 import com.orgsync.api.model.Success;
 
 public class AddAccountsRequest {
 
     public static void main(final String[] args) throws InterruptedException,
             ExecutionException {
         String apiKey = "dd6b9d2beb614611c5eb9f56c34b743d1d86f385";
        String host = "https://api.orgsync.com/api/v2";
         ApiClient client = OrgSync.newApiClient(apiKey, host);
 
         try {
             System.out.println("Requesting orgs");
             OrgsResource resource = client.getResource(Resources.ORGS);
 
             int orgId = 46052;
             List<Integer> ids = Arrays.asList(5666, 1575);
 
             ApiResponse<Success> result = resource.addAccounts(orgId, ids)
                     .get();
             if (result.isSuccess()) {
                 System.out.println(result.getResult());
             } else {
                 System.err.println("Error attempting to retrieve orgs!");
                 System.err.println(result.getError());
             }
             System.out.println("Cleanup client");
         } finally {
             client.destroy();
         }
 
         System.out.println("Exiting...");
     }
 
 }
