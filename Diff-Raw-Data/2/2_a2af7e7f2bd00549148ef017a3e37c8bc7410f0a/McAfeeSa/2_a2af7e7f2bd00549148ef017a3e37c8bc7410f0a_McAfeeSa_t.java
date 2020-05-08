 package edu.uccs.arenger.hilas.security;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.uccs.arenger.hilas.Util;
 import edu.uccs.arenger.hilas.Worker;
 import edu.uccs.arenger.hilas.dal.DalException;
 import edu.uccs.arenger.hilas.dal.Domain;
 import edu.uccs.arenger.hilas.dal.SafeBrowseResult;
 import edu.uccs.arenger.hilas.dal.SafeBrowseResult.Result;
 import edu.uccs.arenger.hilas.dal.Sbs;
 
 public class McAfeeSa extends Worker {
    private static final Logger LOGGER
       = LoggerFactory.getLogger(McAfeeSa.class);
 
    private static final String R_OK      = "siteGreen";
    private static final String R_WARN    = "siteYellow";
    private static final String R_BAD     = "siteRed";
    private static final String R_UNKNOWN = "siteGrey";
 
    private static final String MSA_URL
       = "http://www.siteadvisor.com/sites/%s";
 
    private static final Pattern PAT_VER_TAG = Pattern.compile(
       "<div\\s(.*?id=\"siteVerdict\".*?)>", Pattern.MULTILINE);
    private static final Pattern PAT_VER_STR = Pattern.compile(
       "\\sclass=\"(\\w+)\"");
 
    private static final Pattern PAT_CAT_STR = Pattern.compile(
       "Website Category:</td>\\s*</tr>\\s*<tr>\\s*<td.*?>(.+?)</td>",
       Pattern.MULTILINE);
 
    public long getDelay() {
       return 8; //no published ToS.  We'll assume this is cool.
    }
 
    public TimeUnit getTimeUnit() {
       return TimeUnit.SECONDS;
    }
 
    private Result getVerdict(String html) {
       Result ret = Result.ERROR;
       Matcher m = PAT_VER_TAG.matcher(html);
       if (m.find()) {
          m = PAT_VER_STR.matcher(m.group(1));
          if (m.find()) {
             String r = m.group(1);
             if (r.equals(R_OK)) {
                ret = Result.OK;
             } else if (r.equals(R_WARN)) {
                ret = Result.WARN;
             } else if (r.equals(R_BAD)) {
                ret = Result.BAD;
             } else if (r.equals(R_UNKNOWN)) {
                ret = null;
             } else {
                LOGGER.error("unknown class: {}", r);
             }
          } else {
             LOGGER.error("PAT_VER_STR no match");
          }
       } else {
          LOGGER.error("PAT_VER_TAG no match. html length: {}", html.length());
       }
       return ret;
    }
 
    private String getCategory(String html) {
       String cat = "Error";
       Matcher m = PAT_CAT_STR.matcher(html);
       if (m.find()) {
          cat = m.group(1);
       }
       return cat;
    }
 
    protected void wrappedRun() {
       try {
          List<Domain> doms = Domain.getUnvetted(Sbs.MCAFEE,1);
          if (doms.size() == 0) {
             if (!paused) {
                LOGGER.info("{} - PAUSING (no doms to vet)", this);
                paused = true;
             }
             return;
          }
          if (paused) {
             LOGGER.info("{} - RESUMING", this);
             paused = false;
          }
          LOGGER.debug("submitting 1 domain for vetting");
          String html = Util.getTypedContent(new URL(
            String.format(MSA_URL, doms.get(0).getDomain()))).content;
          SafeBrowseResult sbr = new SafeBrowseResult(
             doms.get(0).getId(), Sbs.MCAFEE);
          sbr.setResult(getVerdict(html));
          sbr.setExtra(getCategory(html));
          sbr.insert();
       } catch (IOException e) {
          LOGGER.error("IOException: {}", e.getMessage());
          paused = true;
       } catch (DalException e) {
          LOGGER.error("dal problem", e);
          paused = true;
       }
    }
 
    public static void main(String[] args) throws Exception {
       if (args.length < 1) {
          System.out.println("McAfeeSa domain");
          System.exit(1);
       }
       McAfeeSa me = new McAfeeSa();
       String html = Util.getTypedContent(new URL(
          String.format(MSA_URL, args[0]))).content;
       System.out.printf("%s - %s\n",
          me.getVerdict(html), me.getCategory(html));
    }
 
 }
