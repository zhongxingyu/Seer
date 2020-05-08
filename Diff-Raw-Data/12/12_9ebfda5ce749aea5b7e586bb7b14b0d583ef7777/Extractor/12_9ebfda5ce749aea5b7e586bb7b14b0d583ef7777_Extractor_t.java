 package com.eads.ariba;
 
 import net.sf.flatpack.DataSet;
 import net.sf.flatpack.DefaultParserFactory;
 import net.sf.flatpack.Parser;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Extractor {
 
     private File exportFile;
 
     public void Extractor() { }
 
     public void Extractor(String exportPath) {
         this.exportFile = new File(exportPath);
     }
 
     public void setExportFile(String exportFile) {
         this.exportFile = new File(exportFile);
     }
 
     public void loadExport() {
         // TODO
         // parsing the report csv file
         // Parser parser = DefaultParserFactory.getInstance().newDelimitedParser(new File("/home/jbonofre/workspace/custom-extractor/Report.20130108.eads-t.full.csv"), ',', '"');
         // DataSet dataSet = parser.parse();
     }
 
     public DefaultHttpClient authentication() throws Exception {
 
         // TODO use slf4j instead of System.out
 
         // create the HTTP client
         DefaultHttpClient httpClient = new DefaultHttpClient();
 
         System.out.println("Auth Step 1: Go on home page ....");
         HttpGet step1Request = new HttpGet("https://s1.ariba.com/Sourcing/Main/ad/loginPage/SSOActions?awsso_ap=ACM&awsr=true&realm=eads-t&awsso_hpk=true");
         HttpResponse step1Response = httpClient.execute(step1Request);
         HttpEntity step1Entity = step1Response.getEntity();
 
         String step1EntityContent = EntityUtils.toString(step1Entity);
 
         System.out.println("Auth Step 2: Get SSOActions ...");
         HttpPost step2Request = new HttpPost("https://s1.ariba.com/Sourcing/Main/ad/login/SSOActions?awr=1&realm=eads-T");
         ArrayList<NameValuePair> step2Params = new ArrayList<NameValuePair>();
         Pattern pattern = Pattern.compile(".*value=\"(.*)\" type=hidden name=ssocc>.*", Pattern.DOTALL);
         Matcher matcher = pattern.matcher(step1EntityContent);
         String ssocc = "";
         if (matcher.matches()) {
             ssocc = matcher.group(1);
         }
 
         step2Params.add(new BasicNameValuePair("ssocc", ssocc));
         step2Params.add(new BasicNameValuePair("timezone", "-120"));
         step2Params.add(new BasicNameValuePair("timezoneFeb", "-120"));
         step2Params.add(new BasicNameValuePair("timezoneAug", "-120"));
         step2Params.add(new BasicNameValuePair("clientTime", "" + System.currentTimeMillis()/1000));
         step2Params.add(new BasicNameValuePair("ApplicationId", "ACM"));
         step2Params.add(new BasicNameValuePair("isLoginForm", "1"));
         step2Params.add(new BasicNameValuePair("passwordadapter", "PasswordAdapter1"));
         step2Params.add(new BasicNameValuePair("awsr", "true"));
         step2Params.add(new BasicNameValuePair("realm", "eads-t"));
         step2Params.add(new BasicNameValuePair("AWLogFilter", "Password"));
         step2Params.add(new BasicNameValuePair("UserName", "Archiving"));
         step2Params.add(new BasicNameValuePair("Password", "Pwd#Apr2013"));
 
         step2Request.setEntity(new UrlEncodedFormEntity(step2Params, HTTP.UTF_8));
 
         HttpResponse step2Response = httpClient.execute(step2Request);
         HttpEntity step2Entity = step2Response.getEntity();
         String step2Content = EntityUtils.toString(step2Entity);
 
         // System.out.println(step2Content);
 
         pattern = Pattern.compile(".*<a.*href='(.*)'>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step2Content);
         String step3Url = "";
         if (matcher.matches()) {
             step3Url = matcher.group(1);
         }
 
         System.out.println("Auth Step 3: Login process and redirect ...");
         HttpGet step3Request = new HttpGet(step3Url);
         HttpResponse step3Response = httpClient.execute(step3Request);
         HttpEntity step3Entity = step3Response.getEntity();
         String step3Content = EntityUtils.toString(step3Entity);
 
         // action
         pattern = Pattern.compile(".*action=\"(.*)\" id=.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4Action = "";
         if (matcher.matches()) {
             step4Action = matcher.group(1);
         }
         // System.out.println("    action: " + step4Action);
 
         // awsso_ar
         pattern = Pattern.compile(".*input value=(.*) type=hidden name=awsso_ar>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4AwssoAr = "";
         if (matcher.matches()) {
             step4AwssoAr = matcher.group(1);
         }
         // System.out.println("    awsso_ar = " + step4AwssoAr);
 
         // awsso_lia
         pattern = Pattern.compile(".*input value=(.*) type=hidden name=awsso_lia>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4AwssoLia = "";
         if (matcher.matches()) {
             step4AwssoLia = matcher.group(1);
         }
         // System.out.println("    awsso_lia = " + step4AwssoLia);
 
         // awsso_up
         pattern = Pattern.compile(".*input value=\"(.*)\" type=hidden name=awsso_up>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4AwssoUp = "";
         if (matcher.matches()) {
             step4AwssoUp = matcher.group(1);
         }
         // System.out.println("    awsso_up = " + step4AwssoUp);
 
         // awsso_ap
         pattern = Pattern.compile(".*input value=(.*) type=hidden name=awsso_ap>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4AwssoAp = "";
         if (matcher.matches()) {
             step4AwssoAp = matcher.group(1);
         }
         // System.out.println("    awsso_ap = " + step4AwssoAp);
 
         // awsso_lu
         pattern = Pattern.compile(".*input value=\"(.*)\" type=hidden name=awsso_lu>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4AwssoLu = "";
         if (matcher.matches()) {
             step4AwssoLu = matcher.group(1);
         }
         // System.out.println("    awsso_lu = " + step4AwssoLu);
 
         // awsso_au
         pattern = Pattern.compile(".*input value=\"(.*)\" type=hidden name=awsso_au>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4AwssoAu = "";
         if (matcher.matches()) {
             step4AwssoAu = matcher.group(1);
         }
         // System.out.println("    awsso_au = " + step4AwssoAu);
 
         // awsso_sl
         pattern = Pattern.compile(".*input value=(.*) type=hidden name=awsso_sl>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4AwssoSl = "";
         if (matcher.matches()) {
             step4AwssoSl = matcher.group(1);
         }
         // System.out.println("    awsso_sl = " + step4AwssoSl);
 
         // awsso_sw
         pattern = Pattern.compile(".*input value=(.*) type=hidden name=awsso_sw>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4AwssoSw = "";
         if (matcher.matches()) {
             step4AwssoSw = matcher.group(1);
         }
         // System.out.println("    awsso_sw = " + step4AwssoSw);
 
         // awsso_ia
         pattern = Pattern.compile(".*input value=(.*) type=hidden name=awsso_ia>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4AwssoIa = "";
         if (matcher.matches()) {
             step4AwssoIa = matcher.group(1);
         }
         // System.out.println("    awsso_ia = " + step4AwssoIa);
 
         // passwordadapter
         pattern = Pattern.compile(".*input value=(.*) type=hidden name=passwordadapter>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4Passwordadapter = "";
         if (matcher.matches()) {
             step4Passwordadapter = matcher.group(1);
         }
         // System.out.println("    passwordadapter = " + step4Passwordadapter);
 
         // awsso_fl
         pattern = Pattern.compile(".*input value=(.*) type=hidden name=awsso_fl>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4AwssoFl = "";
         if (matcher.matches()) {
             step4AwssoFl = matcher.group(1);
         }
         // System.out.println("    awsso_fl = " + step4AwssoFl);
 
         // realm
         pattern = Pattern.compile(".*input value=\"(.*)\" type=hidden name=realm>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4Realm = "";
         if (matcher.matches()) {
             step4Realm = matcher.group(1);
         }
         // System.out.println("    realm = " + step4Realm);
 
         // awsso_ka
         pattern = Pattern.compile(".*input value=\"(.*)\" type=hidden name=awsso_ka>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4AwssoKa = "";
         if (matcher.matches()) {
             step4AwssoKa = matcher.group(1);
         }
         // System.out.println("    awsso_ka = " + step4AwssoKa);
 
         // awsso_kt
         pattern = Pattern.compile(".*input value=(.*) type=hidden name=awsso_kt>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step3Content);
         String step4AwssoKt = "";
         if (matcher.matches()) {
             step4AwssoKt = matcher.group(1);
         }
         // System.out.println("    awsso_kt = " + step4AwssoKt);
 
         System.out.println("Auth Step 4: Redirect to workspace ...");
         HttpPost step4Request = new HttpPost(step4Action);
         ArrayList<NameValuePair> step4Params = new ArrayList<NameValuePair>();
         step4Params.add(new BasicNameValuePair("awsso_ar", step4AwssoAr));
         step4Params.add(new BasicNameValuePair("awsso_lia", step4AwssoLia));
         step4Params.add(new BasicNameValuePair("awsso_up", step4AwssoUp));
         step4Params.add(new BasicNameValuePair("awsso_ap", step4AwssoAp));
         step4Params.add(new BasicNameValuePair("awsso_lu", step4AwssoLu));
         step4Params.add(new BasicNameValuePair("awsso_au", step4AwssoAu));
         step4Params.add(new BasicNameValuePair("awsso_sl", step4AwssoSl));
         step4Params.add(new BasicNameValuePair("awsso_sw", step4AwssoSw));
         step4Params.add(new BasicNameValuePair("awsso_ia", step4AwssoIa));
         step4Params.add(new BasicNameValuePair("passwordadapter", step4Passwordadapter));
         step4Params.add(new BasicNameValuePair("awsso_fl", step4AwssoFl));
         step4Params.add(new BasicNameValuePair("realm", step4Realm));
         step4Params.add(new BasicNameValuePair("awsso_ka", step4AwssoKa));
         step4Params.add(new BasicNameValuePair("awsso_kt", step4AwssoKt));
 
         step4Request.setEntity(new UrlEncodedFormEntity(step4Params, HTTP.UTF_8));
         HttpResponse step4Response = httpClient.execute(step4Request);
         HttpEntity step4Entity = step4Response.getEntity();
         String step4Content = EntityUtils.toString(step4Entity);
 
         System.out.println("Auth Step 5: AWRedirect");
         pattern = Pattern.compile(".*<a id='AWRedirect' href='(.*)'>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step4Content);
         String step5Action = "";
         if (matcher.matches()) {
             step5Action = matcher.group(1);
         }
         step5Action = "https://s1.ariba.com" + step5Action;
 
         HttpGet step5Request = new HttpGet(step5Action);
         HttpResponse step5Response = httpClient.execute(step5Request);
         HttpEntity step5Entity = step5Response.getEntity();
         String step5Content = EntityUtils.toString(step5Entity);
 
         // System.out.println(step5Content);
 
         return httpClient;
     }
 
     public void extractDocument(String workspaceId, String eventId, DefaultHttpClient httpClient) throws Exception {
 
         System.out.println("Document Extract Step 1: Go on viewDocument page");
         HttpGet step1Request = new HttpGet("https://s1.ariba.com/Sourcing/Main/ad/viewDocument?realm=eads-t&ID=" + eventId);
         HttpResponse step1Response = httpClient.execute(step1Request);
         HttpEntity step1Entity = step1Response.getEntity();
         String step1Content = EntityUtils.toString(step1Entity);
 
         System.out.println(step1Content);
 
         System.out.println("Document Extract Step 2: Generate Excel export");
         HttpPost step2Request = new HttpPost("https://s1.ariba.com/Sourcing/Main/aw");
         ArrayList<NameValuePair> step2Params = new ArrayList<NameValuePair>();
         step2Params.add(new BasicNameValuePair("_hsrspc", "1"));
     }
 
     public void extractWorkspace(String workspaceId, DefaultHttpClient httpClient) throws Exception {
 
         System.out.println("Workspace Extract Step 1: Go on viewDocument page");
         HttpGet step1Request = new HttpGet("https://s1.ariba.com/Sourcing/Main/ad/viewDocument?realm=eads-t&ID=" + workspaceId);
         HttpResponse step1Response = httpClient.execute(step1Request);
         HttpEntity step1Entity = step1Response.getEntity();
         String step1Content = EntityUtils.toString(step1Entity);
 
         // System.out.println(step1Content);
 
         System.out.println("Workspace Extract Step 2: Create workspaceId folder");
         File workspaceFolder = new File(workspaceId);
         if (!workspaceFolder.exists()) {
             workspaceFolder.mkdirs();
         }
         File attachmentsFolder = new File(workspaceFolder, "ATTACHMENTS");
         if (!attachmentsFolder.exists()) {
             attachmentsFolder.mkdirs();
         }
 
         System.out.println("Workspace Extract Step 3: Switching to Overview tab");
         // extract the awssk
         Pattern pattern = Pattern.compile(".*/Sourcing/Main/aw\\?awh=r&awssk=(.*)&realm=eads-T','/Sourcing/Main/ad/ping.*", Pattern.DOTALL);
         Matcher matcher = pattern.matcher(step1Content);
         String awssk = "";
         if (matcher.matches()) {
             awssk = matcher.group(1);
         }
         HttpPost step3Request = new HttpPost("https://s1.ariba.com/Sourcing/Main/aw");
         ArrayList<NameValuePair> step3Params = new ArrayList<NameValuePair>();
         step3Params.add(new BasicNameValuePair("_6sokfd", "0"));
         step3Params.add(new BasicNameValuePair("_et2lqc", "false"));
         step3Params.add(new BasicNameValuePair("_npf_3", "0"));
         step3Params.add(new BasicNameValuePair("_utyjxd", "0"));
         step3Params.add(new BasicNameValuePair("awcharset", "UTF-8"));
         step3Params.add(new BasicNameValuePair("awfa", "_yhm$xd"));
         step3Params.add(new BasicNameValuePair("awfid", "true"));
         step3Params.add(new BasicNameValuePair("awii", "xmlhttp"));
         step3Params.add(new BasicNameValuePair("awr", "a"));
         step3Params.add(new BasicNameValuePair("awsl", "0"));
         step3Params.add(new BasicNameValuePair("awsn", "_hcftcc"));
         step3Params.add(new BasicNameValuePair("awsnf", "_fmkphb"));
         step3Params.add(new BasicNameValuePair("awst", "0"));
         step3Params.add(new BasicNameValuePair("awssk", awssk));
         step3Request.setEntity(new UrlEncodedFormEntity(step3Params));
         HttpResponse step3Response = httpClient.execute(step3Request);
         HttpEntity step3Entity = step3Response.getEntity();
         String step3Content = EntityUtils.toString(step3Entity);
 
         // System.out.println(step3Content);
 
         System.out.println("Workspace Extract Step 4: Extracting from Overview tab");
         Properties overview = new Properties();
 
         // ID
         pattern = Pattern.compile(".*<tr><td height=16 width=\"1%\" nowrap=true class=project_summary_label>ID:</td><td></td>.<td style=\"padding-bottom:2px;\" nowrap=true align=left>(.*)</td></tr>.<tr><td height=16 width=\"1%\" nowrap=true class=project_summary_label>.*", Pattern.DOTALL);
        matcher = pattern.matcher(step3Content);
         String id = "";
         if (matcher.matches()) {
             id = matcher.group(1);
         }
 
         overview.put("ID", id);
 
         // Version
        pattern = Pattern.compile(".*<td nowrap class=ffl><label for=.......>Version:</label></td>.<td class=ffi><span id=.......><img alt=\"\" border=0 height=11 width=10 src=\"https://s1.ariba.com/Sourcing/Main/ad/awres/eads-t/new3/.../cleardot.gif\"></span></td>.<td class=ffp>..(.*)..</td>.</tr>.........<tr id=...... valign=middle bh=ROV>.<td nowrap class=ffl>.*", Pattern.DOTALL);
        matcher = pattern.matcher(step3Content);
         String version = "";
         if (matcher.matches()) {
             version = matcher.group(1);
         }
 
         overview.put("Version", version.trim());
 
         // Project State
        pattern = Pattern.compile(".*Project&nbsp;State:</label></td>.<td class=ffi><span id=......><img alt=\"\" border=0 height=11 width=10 src=\"https://s1.ariba.com/Sourcing/Main/ad/awres/eads-t/new3/.../cleardot.gif\"></span></td>.<td class=ffp>.<table class=\"cueT\" cellpadding=\"0\"><tr>.<td>.(.*)..</td>.<td><div class=rr id=.......>..<a href=\"#\" class=\"hoverArrow noLine\" _mid=...... bh=PML id=.......><img alt=\"\" border=0 height=12 width=15 src=\"https://s1.ariba.com/Sourcing/Main/ad/awres/eads-t/new3/.../cleardot.gif\" class=cueTipIcon></a>.<div style=\"display:none;\" class=awmenu id=...... _reloc=1 _ondisplay=\"ariba.Widgets.sizeMsgDiv\\(elm\\)\">.<div><img width=\"0\" height=\"0\" border=\"0\"/></div>..<div class=rr id=.......><div awneedsLoading=true class=lazyLoading id=......>Loading.*", Pattern.DOTALL);
        matcher = pattern.matcher(step3Content);
         String projectState = "";
         if (matcher.matches()) {
             projectState = matcher.group(1);
         }
 
         overview.put("ProjectState", projectState.trim());
 
         // TODO continue the other fields
 
         System.out.println("... store the overview properties");
         // store the overview properties
         FileWriter writer = new FileWriter(new File(workspaceFolder, "overview.properties"));
         overview.store(writer, null);
         writer.close();
 
         System.out.println("Workspace Extract Step 5: Switching to Document tab");
         HttpPost step5Request = new HttpPost("https://s1.ariba.com/Sourcing/Main/aw");
         ArrayList<NameValuePair> step5Params = new ArrayList<NameValuePair>();
         step5Params.add(new BasicNameValuePair("PageErrorPanelIsMinimized", "false"));
         step5Params.add(new BasicNameValuePair("awcharset", "UTF-8"));
         step5Params.add(new BasicNameValuePair("awfa", "_yhm$xd"));
         step5Params.add(new BasicNameValuePair("awii", "xmlhttp"));
         step5Params.add(new BasicNameValuePair("awr", "6"));
         step5Params.add(new BasicNameValuePair("awsl", "0"));
         step5Params.add(new BasicNameValuePair("awsn", "_6lxl_d"));
         step5Params.add(new BasicNameValuePair("awsnf", "_fmkphb"));
         step5Params.add(new BasicNameValuePair("awssk", awssk));
         step5Params.add(new BasicNameValuePair("awst", "0"));
         step5Request.setEntity(new UrlEncodedFormEntity(step5Params));
         HttpResponse step5Response = httpClient.execute(step5Request);
         HttpEntity step5Entity = step5Response.getEntity();
         String step5Content = EntityUtils.toString(step5Entity);
 
         System.out.println(step5Content);
 
         /*
         // get awfa
         pattern = Pattern.compile(".*<input value=(.*) type=hidden name=awfa>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step1Content);
         String awfa = "";
         if (matcher.matches()) {
             awfa = matcher.group(1);
         }
         System.out.println("awfa: " + awfa);
         // get awsnf
         pattern = Pattern.compile(".*<input value=(.*) type=hidden name=awsnf>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step1Content);
         String awsnf = "";
         if (matcher.matches()) {
             awsnf = matcher.group(1);
         }
         System.out.println("awsnf: " + awsnf);
         // get awssk
         pattern = Pattern.compile(".*awssk=(.*)',4000,.*", Pattern.DOTALL);
         matcher = pattern.matcher(step1Content);
         String awssk = "";
         if (matcher.matches()) {
             awssk = matcher.group(1);
         }
         System.out.println("awssk: " + awssk);
 
 
         HttpPost step3Request = new HttpPost("https://s1.ariba.com/Sourcing/Main/aw");
         ArrayList<NameValuePair> step3Params = new ArrayList<NameValuePair>();
         step3Params.add(new BasicNameValuePair("PageErrorPanelIsMinimized", "0"));
         step3Params.add(new BasicNameValuePair("awcharset", "UTF-8"));
         step3Params.add(new BasicNameValuePair("awfid", "true"));
         step3Params.add(new BasicNameValuePair("awii", "xmlhttp"));
         step3Params.add(new BasicNameValuePair("awr", "g"));
         step3Params.add(new BasicNameValuePair("awsl", "0"));
         */
 
         /*
         System.out.println("Step 6: Click on Search / Sourcing Project menu item");
         // search menu ID
         pattern = Pattern.compile(".*<a href=\"#\" class=\"\" _mid=AMBSearch bh=PML id=(.*)>Search</a>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step5Content);
         String step6SearchMenuId = "";
         if (matcher.matches()) {
             step6SearchMenuId = matcher.group(1);
         }
         // System.out.println("    Search Menu ID = " + step6SearchMenuId);
 
         // sourcing project menu ID
         pattern = Pattern.compile(".*<a id=(.*) bh=PMI class=\"mC\" href=\"#\" tabIndex=\"-1\">Sourcing Project</a>.*", Pattern.DOTALL);
         matcher = pattern.matcher(step5Content);
         String step6SourcingProjectMenuId = "";
         if (matcher.matches()) {
             step6SourcingProjectMenuId = matcher.group(1);
         }
         // System.out.println("    Sourcing Project Menu ID = " + step6SourcingProjectMenuId);
 
         // awssk
         pattern = Pattern.compile(".*awssk=(.*)&realm.*");
         matcher = pattern.matcher(step5Action);
         String awssk = "";
         if (matcher.matches()) {
             awssk = matcher.group(1);
         }
         // System.out.println("    awssk = " + awssk);
 
         System.out.println("Step 6: https://s1.ariba.com/Sourcing/Main/aw?awii=xmlhttp&awr=9&awsl=0&awst=0&awssk=" + awssk + "&awsn=" + step6SearchMenuId + "," + step6SourcingProjectMenuId + " ...");
 
         HttpGet step1Request = new HttpGet("https://s1.ariba.com/Sourcing/Main/aw?awii=xmlhttp&awr=9&awsl=0&awst=0&awssk=" + awssk + "&awsn=" + step6SearchMenuId + "," + step6SourcingProjectMenuId);
         HttpResponse step1Response = httpClient.execute(step1Request);
         HttpEntity step1Entity = step1Response.getEntity();
         String step1Content = EntityUtils.toString(step1Entity);
 
         // System.out.println(step1Content);
 
         //
         // Step 7: click on search
         //
         System.out.println("Step 7: Click on ALL search");
         HttpGet step7Request = new HttpGet("https://s1.ariba.com/Sourcing/Main/aw?awr=t&awssk=" + awssk + "&awsn=_ah1d0d&awst=84&awsl=0&awii=xmlhttp");
         HttpResponse step7Response = httpClient.execute(step7Request);
         HttpEntity step7Entity = step7Response.getEntity();
         String step7Content = EntityUtils.toString(step7Entity);
 
         System.out.println(step7Content);
         */
     }
 
     public static void main(String[] args) throws Exception {
 
         String workspaceId = "WS10448415";
         String eventId = "Doc19171669";
 
         Extractor extractor = new Extractor();
 
         DefaultHttpClient httpClient = extractor.authentication();
         extractor.extractWorkspace(workspaceId, httpClient);
         extractor.extractDocument(workspaceId, eventId, httpClient);
     }
 
 }
