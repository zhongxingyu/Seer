 package uk.ac.ebi.sampletab;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 
 import uk.ac.ebi.age.admin.shared.Constants;
 import uk.ac.ebi.age.admin.shared.SubmissionConstants;
 import uk.ac.ebi.age.ext.submission.Status;
 
 import com.pri.util.StringUtils;
 import com.pri.util.stream.StreamPump;
 
 public class STLoader
 {
  static final String usage = "java -jar STLoader.jar [options...] <input file/dir> [ ... <input file/dir> ]";
  /**
   * @param args
   */
  public static void main(String[] args)
  {
   Options options = new Options();
   CmdLineParser parser = new CmdLineParser(options);
 
   try
   {
    parser.parseArgument(args);
   }
   catch(CmdLineException e)
   {
    System.err.println(e.getMessage());
    System.err.println(usage);
    parser.printUsage(System.err);
    return;
   }
   
   if( options.getDirs() == null || options.getDirs().size() == 0  )
   {
    System.err.println(usage);
    parser.printUsage(System.err);
    return;
   }
   
   
   List<File> infiles = new ArrayList<File>();
   
  
   for( String outf : options.getDirs() )
   {
    File in = new File( outf );
    
    
    if( in.isDirectory() )
     infiles.addAll( Arrays.asList( in.listFiles() ) );
    else if( in.isFile() )
     infiles.add( in );
    else
    {
     System.err.println("Input file/directory '"+in.getAbsolutePath()+"' doesn't exist");
     return;
    }
   }
   
   
   if( infiles.size() == 0 )
   {
    System.err.println("No files to process");
    return;
   }
   
   boolean remote = options.isLoad() || options.isStore();
  
   if( options.getOutDir() == null )
   {
    System.err.println("Output directory is not specified");
    return;
   }
   
   File outfile = new File( options.getOutDir() );
 
   if( outfile.isFile() )
   {
    System.err.println("Output path should point to a directory");
    return;
   }
   
   if( ! outfile.exists() && ! outfile.mkdirs() )
   {
    System.err.println("Can't create output directory");
    return;
   }
   
   String sessionKey = null;
   DefaultHttpClient httpclient = null;
   
   PrintWriter log = null;
   
   try
   {
    log = new PrintWriter( new File(outfile,".log") );
   }
   catch(FileNotFoundException e1)
   {
    System.err.println("Can't create log file: "+new File(outfile,".log").getAbsolutePath());
    return;
   }
   
   if( remote )
   {
    if( options.getDatabaseURL() == null )
    {
     System.err.println("Database URI is required for remote operations");
     return;
    }
    else if(  ! options.getDatabaseURL().endsWith("/") )
     options.setDatabaseURI( options.getDatabaseURL()+"/" );
    
    if( options.getUser() == null )
    {
     System.err.println("User name is required for remote operations");
     return;
    }
    
    boolean ok = false;
    
    try
    {
 
     httpclient = new DefaultHttpClient();
     HttpPost httpost = new HttpPost(options.getDatabaseURL()+"Login");
     
     List<NameValuePair> nvps = new ArrayList<NameValuePair>();
     nvps.add(new BasicNameValuePair("username", options.getUser() ));
     nvps.add(new BasicNameValuePair("password", options.getPassword()!=null?options.getPassword():""));
     
     httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
     
     log.println("Trying to login onto the server");
     
     HttpResponse response = httpclient.execute(httpost);
     
     if( response.getStatusLine().getStatusCode() != HttpStatus.SC_OK )
     {
      log.println("Server response code is: "+response.getStatusLine().getStatusCode());
      return;
     }
     
     HttpEntity ent = response.getEntity();
     
     String respStr = EntityUtils.toString( ent ).trim();
     
     if( respStr.startsWith("OK:") )
     {
      System.out.println("Login successful");
      log.println("Login successful");
      sessionKey = respStr.substring(3);
     }
     else
     {
      log.println("Login failed: "+respStr);
      return;
     }
     
     EntityUtils.consume(ent);
     
     ok=true;
    }
    catch(Exception e)
    {
     log.println("ERROR: Login failed: "+e.getMessage());
     log.close();
     
     return;
    }
    finally
    {
     if( ! ok )
     {
      httpclient.getConnectionManager().shutdown();
      System.err.println("Login failed");
     }
    }
   }
   
   File atDir = null;
   
   if( ( ! remote ) || options.isSave() )
   {
    atDir = new File(outfile,"age-tab");
    atDir.mkdir();
   }
 
   File respDir = null;
   
   if( remote && options.isSaveResponse() )
   {
    respDir = new File(outfile,"server");
    respDir.mkdir();
   }
    
   try
   {
    for(File stfile : infiles)
    {
     Submission s = null;
 
     long time = System.currentTimeMillis();
 
     System.out.println("Parsing file: " + stfile);
     log.println("Parsing file: " + stfile);
 
     String stContent = null;
     try
     {
      stContent = StringUtils.readUnicodeFile(stfile);
      s = STParser3.readST(stContent);
     }
     catch(Exception e)
     {
      System.out.println("ERROR. See log file for details");
      log.println("File parsing error: " + e.getMessage());
      e.printStackTrace();
      continue;
     }
 
     log.println("Parsing success. " + (System.currentTimeMillis() - time) + "ms");
 
     time = System.currentTimeMillis();
 
     System.out.println("Converting to AGE-TAB");
     log.println("Converting to AGE-TAB");
 
     ByteArrayOutputStream atOut = new ByteArrayOutputStream();
 
     try
     {
      ATWriter.writeAgeTab(s, atOut);
 
      atOut.close();
     }
     catch(IOException e)
     {
     }
 
     byte[] atContent =atOut.toByteArray();
     
     atOut=null;
     
     if((!remote) || options.isSave())
     {
      File atOutFile = new File(atDir, stfile.getName() + ".age.txt");
 
      try
      {
       FileOutputStream fos = new FileOutputStream(atOutFile);
       
       StreamPump.doPump(new ByteArrayInputStream(atContent), fos, true);
      }
      catch(IOException e)
      {
       log.println("ERROR: Can't write AGE-TAB file: "+atOutFile.getAbsolutePath()+" Reason: "+e.getMessage());
       return;
      }
     }
 
     HttpPost post = new HttpPost( options.getDatabaseURL()+"upload?"+Constants.sessionKey+"="+sessionKey );
 
     String key = String.valueOf(System.currentTimeMillis());
     
     if(remote)
     {
      MultipartEntity reqEntity = new MultipartEntity();
 
      String sbmId = s.getAnnotation(Definitions.SUBMISSIONIDENTIFIER).getValue();
 
      try
      {
 
       Status sts;
 
       if( options.isNewSubmissions() )
        sts = Status.NEW;
       else if( options.isUpdateSubmissions() )
        sts = Status.UPDATE;
       else
        sts = Status.UPDATEORNEW;
 
      reqEntity.addPart(Constants.uploadHandlerParameter, new StringBody(SubmissionConstants.SUBMISSON_COMMAND));
       reqEntity.addPart(SubmissionConstants.SUBMISSON_KEY, new StringBody(key));
       reqEntity.addPart(SubmissionConstants.SUBMISSON_STATUS, new StringBody(sts.name()) );
 
       if(sbmId != null)
       {
        reqEntity.addPart(SubmissionConstants.SUBMISSON_ID,
          new StringBody(sbmId));
       
        reqEntity.addPart(SubmissionConstants.MODULE_ID+"1", new StringBody(sbmId+"_MOD") );
       }
       else
        sbmId = key;
 
       if(!options.isStore())
        reqEntity.addPart(SubmissionConstants.VERIFY_ONLY, new StringBody("on"));
 
       String descr = s.getAnnotation(Definitions.SUBMISSIONDESCRIPTION).getValue();
 
       if(descr != null)
        reqEntity.addPart(SubmissionConstants.SUBMISSON_DESCR, new StringBody(descr));
 
       reqEntity.addPart(SubmissionConstants.MODULE_DESCRIPTION + "1", new StringBody("AGE-TAB file"));
       
   
       reqEntity.addPart(SubmissionConstants.MODULE_STATUS + "1", new StringBody(sts.name()));
 
       reqEntity.addPart(SubmissionConstants.ATTACHMENT_DESC + "2", new StringBody("SAMPLE-TAB file"));
       reqEntity.addPart(SubmissionConstants.ATTACHMENT_STATUS + "2", new StringBody(sts.name()));
       reqEntity.addPart(SubmissionConstants.ATTACHMENT_ID + "2", new StringBody("SAMPLE-TAB"));
 
       reqEntity.addPart(SubmissionConstants.MODULE_FILE + "1", new ByteArrayBody(atContent,
         "text/plain; charset=UTF-8", sbmId + ".age.txt"));
 
       reqEntity.addPart(SubmissionConstants.ATTACHMENT_FILE + "2", new ByteArrayBody(stContent.getBytes("UTF-8"),
         "text/plain; charset=UTF-8", sbmId + ".sampletab.txt"));
    
      }
      catch(UnsupportedEncodingException e)
      {
       log.println("ERROR: UnsupportedEncodingException: " + e.getMessage());
       return;
      }
 
      post.setEntity(reqEntity);
 
      HttpResponse response;
      try
      {
       response = httpclient.execute(post);
 
       if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
       {
        log.println("Server response code is: " + response.getStatusLine().getStatusCode());
        return;
       }
 
       HttpEntity ent = response.getEntity();
 
       String respStr = EntityUtils.toString(ent);
 
       int pos = respStr.indexOf("OK-" + key);
 
       if(pos == -1)
       {
        log.println("ERROR: Invalid server response : " + respStr);
        continue;
       }
 
       pos = pos + key.length() + 5;
       String respStat = respStr.substring(pos, respStr.indexOf(']', pos));
 
       log.println("Submission status: " + respStat);
       System.out.println("Submission status: " + respStat);
 
       if(options.isSaveResponse())
       {
        log.println("Writing response");
        File rspf = new File(respDir, stfile.getName() + '.' + respStat);
 
        PrintWriter pw = new PrintWriter(rspf, "UTF-8");
 
        pw.write(respStr);
        pw.close();
       }
 
       EntityUtils.consume(ent);
      }
      catch(Exception e)
      {
       log.println("ERROR: IO error: " + e.getMessage());
       return;
      }
     }
 
     log.println("File '"+stfile.getName()+ "' done");
     System.out.println("File '"+stfile.getName()+ "' done");
 
    }
   }
   finally
   {
    if(httpclient != null)
     httpclient.getConnectionManager().shutdown();
 
    log.close();
   }
   
  }
 
 }
