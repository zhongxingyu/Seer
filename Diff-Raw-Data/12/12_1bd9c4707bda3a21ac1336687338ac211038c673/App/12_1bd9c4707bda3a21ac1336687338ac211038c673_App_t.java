 package com.eyedsecure.client;
 
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 
 /**
  * Created with IntelliJ IDEA.
  * User: boksman
  * Date: 2/17/13
  * Time: 11:23 PM
  */
 public class App {
 
     private static final int TOKEN_ID_MIN_LEN = 14;
     private static final int TOKEN_ID_MAX_LEN = 14;
 
     private static final int PROFILE_ID_MIN_LEN = 1;
     private static final int PROFILE_ID_MAX_LEN = 32;
 
 
     private static final int OTP_MIN_LEN = 5;
     private static final int OTP_MAX_LEN = 12;
 
     private static final int MIN_DPI = 300;
     private static final int MAX_DPI = 3000;
 
     private static final int MIN_COLOR_ADJ = -50;
     private static final int MAX_COLOR_ADJ = +50;
 
     public static void main(String args[]) throws Exception {
         String tokenId = null;
         String clientId= null;
         String sharedKey = null;
         String challengeId = null;
 
         String imageProfileId = null;
         String otp = null;
 
         String path = "c:\\";
 
         Integer dpiX = null, dpiY = null;
         Integer red = null, green = null, blue = null;
 
 
         for (int i = 0; i < args.length; i++) {
             if (args[i].equals("-tid") || args[i].equals("--token-id")) {
                 // Token Id
                 tokenId = args[i + 1];
             } else if (args[i].equals("-c") || args[i].equals("--client-id")) {
                 // Client API Id
                 clientId = args[i + 1];
 
             } else if (args[i].equals("-cid") || args[i].equals("--challenge-id")) {
                 // Challenge id
                 challengeId = args[i + 1];
 
             } else if (args[i].equals("-sk") || args[i].equals("--shared-key")) {
                 // Shared Secret Key
                 sharedKey = args[i + 1];
             } else if (args[i].equals("-pid") || args[i].equals("--image-profile-id")) {
                 // Image settings profile id, for saved resolutions
                 imageProfileId = args[i + 1];
             } else if (args[i].equals("-otp") || args[i].equals("--otp")) {
                 // One Time password for validations
                 otp = args[i + 1];
             } else if (args[i].equals("-dx") || args[i].equals("--dpi-x")) {
                 // Dots per inch x
                 dpiX = Integer.parseInt(args[i + 1]);
             } else if (args[i].equals("-dy") || args[i].equals("--dpi-y")) {
                 // Dots per inch y
                 dpiY = Integer.parseInt(args[i + 1]);
             } else if (args[i].equals("-p") || args[i].equals("--path")) {
                 // Path to store challenge image files
                 path = args[i + 1];
             }
         }
 
 
         if (tokenId == null || tokenId.length() > TOKEN_ID_MAX_LEN || tokenId.length() < TOKEN_ID_MIN_LEN) {
             System.err.println("Error: Invalid or missing public-id");
             printUsage();
             return;
         }
 
         if (clientId == null || clientId.length()!=16) {
             System.err.println("Error: Invalid or missing client-id. Must be 16 chars long.");
             printUsage();
             return;
         }
 
 
         String cmd = args[args.length - 1];
 
         if (cmd.equals("rc") || cmd.equals("request-challenge")) {
             if (imageProfileId != null && (imageProfileId.length() > PROFILE_ID_MAX_LEN || imageProfileId.length() < TOKEN_ID_MIN_LEN)) {
                 System.err.println("Error: Invalid image-profile-id");
                 printUsage();
                 return;
             }
 
             requestChallenge(clientId, tokenId, sharedKey, path);
 
         } else if (cmd.equals("activate")) {
             activate(clientId, tokenId, sharedKey);
         } else if (cmd.equals("deactivate")) {
             deactivate(clientId, tokenId, sharedKey);
 
         } else if (cmd.equals("val") || cmd.equals("validate")) {
 
             if (otp == null || otp.length() > OTP_MAX_LEN || otp.length() < OTP_MIN_LEN) {
                 System.err.println("Error: Invalid or missing otp");
                 printUsage();
                 return;
             }
 
             validateOTP(clientId, tokenId, sharedKey, otp, challengeId);
 
         } else {
             System.err.println("Error: Missing or invalid command");
             printUsage();
         }
 
         System.exit(0);
     }
 
 
     /**
      * Request a challenge image
      * @param clientId    - API Key ID
      * @param tokenId    - Token Reg code written on token
      * @param sharedKey  - Shared API Key
      * @param outputPath - Path to store image
      *
      * todo: add support for imageProfileId
      * todo: add support for jpegs and pngs
      *
      * @throws Exception
      */
     private static void requestChallenge(String clientId, String tokenId, String sharedKey, String outputPath) throws Exception{
         EyeDSecureClient c = new EyeDSecureClient(clientId, sharedKey);
         Response response = c.requestChallenge(tokenId);
 
 
         if (response != null && response.getResponseCode() == ResponseCode.SUCCESS) {
             System.out.println("Token Activated");
         } else {
             System.out.println("Failed to activate token");
         }
 
 
         if (response != null && response.getResponseCode() == ResponseCode.SUCCESS) {
             System.out.println("Token image written to file");
 
             OutputStream out = new FileOutputStream(outputPath + "challenge.png");
             out.write(response.getImage());
             out.flush();
             out.close();
 
 
         } else {
             System.out.println("Request Failed");
         }
 
 
 
 
     }
 
     /**
      * Activate a token
      * @param clientId - API Key ID
      * @param tokenId   - Token Reg code written on token
      * @param sharedKey - Shared API Key
      * @throws Exception
      */
     private static void activate(String clientId, String tokenId, String sharedKey) throws Exception {
         EyeDSecureClient c = new EyeDSecureClient(clientId, sharedKey);
         Response response = c.activate(tokenId, true);
 
 
         if (response != null && response.getResponseCode() == ResponseCode.SUCCESS) {
             System.out.println("Token Activated");
         } else {
             System.out.println("Failed to activate token " + response);
         }
 
 
     }
 
 
     /**
      * Deactivate the token
      * @param clientId - API Key ID
      * @param tokenId   - Token Reg code written on token
      * @param sharedKey - Shared API Key
      * @throws Exception
      * */
     private static void deactivate(String clientId, String tokenId, String sharedKey) throws Exception {
         EyeDSecureClient c = new EyeDSecureClient(clientId, sharedKey);
         Response response = c.activate(tokenId, false);
 
         System.out.println("Response: " + response);
 
         if (response != null && response.getResponseCode() == ResponseCode.SUCCESS) {
             System.out.println("Token Activated");
         } else {
             System.out.println("Failed to activate token");
         }
     }
 
 
     /**
      * Validate given OTP for given challengeID
      *      *
      * @param clientId       - API Key ID
      * @param tokenId        - Token Reg code written on token
      * @param sharedKey      - Shared API Key
      * @param otp            - One-time password to validate
      * @param challengeId    - Optional challenge id to validate (Optional)
      */
     private static void validateOTP(String clientId, String tokenId, String sharedKey, String otp, String challengeId) throws Exception {
         EyeDSecureClient c = new EyeDSecureClient(clientId, sharedKey);
         Response response = c.validateOTP(tokenId, otp, challengeId);
 
         System.out.println("Response: " + response);
 
         if (response != null && response.getResponseCode() == ResponseCode.SUCCESS) {
             System.out.println("OTP Validated");
         } else {
             System.out.println("Failed");
         }
     }
 
 
     private static void printUsage() {
         System.err.println("\nTest Eye-D Secure client functions against Eye-D Secure server");
         System.err.println("\nUsage: java -jar client.jar <options> <command>");
 
        System.err.println("\n    java -jar client.jar -tid XXXXXYYYYZZZZZ -c XXXXXXXXXXXXXXXX -sk xxxxxxxxxxxx activate");
 
         System.err.println("\n\nCommands:");
         System.err.println("request-challenge - Request a challenge image from the server");
         System.err.println("validate - Validate a one-time (OTP) password given challenge id");
         System.err.println("activate - Activate given tokenId");
         System.err.println("deactivate - Deactivate given tokenId");
 
         System.err.println("\n\nOptions:");
         System.err.println("-tid,   --token-id              - Eye-D Card public token id");
         System.err.println("-c,     --client-id             - API Key id");
         System.err.println("-cid,   --challenge-id          - API Key id");
         System.err.println("-sk,    --shared-key            - API Key shared secret, must be obtained from website");
         System.err.println("-p,     --path                  - Path to store challenge images");
         System.err.println("-pid,   --image-profile-id      - Image profile id, identifier referring to saved profile defining dpi-x, dpi-y, rgb color profile, etc");
         System.err.println("-otp                            - One-time password");
         System.err.println("-dx,    --dpi-x                 - Horizontal dots per inch for given image profile");
         System.err.println("-dy,    --dpi-y                 - Vertical dots per inch for given image profile");
         System.err.println("-r,     --red                   - Red adjustment");
         System.err.println("-g,     --green                 - Green adjustment");
         System.err.println("-b,     --blue                  - Blue adjustment");
 
 
         System.err.println("\n\nPlace your Eye-D Card over the challenge image to determine OTP");
 
     }
 }
 
