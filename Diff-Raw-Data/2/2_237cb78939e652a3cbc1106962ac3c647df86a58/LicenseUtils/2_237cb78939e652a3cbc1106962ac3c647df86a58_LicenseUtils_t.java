 package gov.nih.nci.evs.browser.utils;
 
 import java.util.*;
 import javax.servlet.http.*;
 import org.apache.log4j.*;
 import gov.nih.nci.evs.browser.bean.*;
 
 /**
  * <!-- LICENSE_TEXT_START -->
  * Copyright 2008,2009 NGIT. This software was developed in conjunction
  * with the National Cancer Institute, and so to the extent government
  * employees are co-authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *   1. Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the disclaimer of Article 3,
  *      below. Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials provided
  *      with the distribution.
  *   2. The end-user documentation included with the redistribution,
  *      if any, must include the following acknowledgment:
  *      "This product includes software developed by NGIT and the National
  *      Cancer Institute."   If no such end-user documentation is to be
  *      included, this acknowledgment shall appear in the software itself,
  *      wherever such third-party acknowledgments normally appear.
  *   3. The names "The National Cancer Institute", "NCI" and "NGIT" must
  *      not be used to endorse or promote products derived from this software.
  *   4. This license does not authorize the incorporation of this software
  *      into any third party proprietary programs. This license does not
  *      authorize the recipient to use any trademarks owned by either NCI
  *      or NGIT
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
  *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
  *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
  *      NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
  *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *      POSSIBILITY OF SUCH DAMAGE.
  * <!-- LICENSE_TEXT_END -->
  */
 
 /**
  * @author EVS Team
  * @version 1.0
  */
 
 public class LicenseUtils {
     private static Logger _logger = Logger.getLogger(LicenseUtils.class);
 
     private static LicenseBean getLicenseBean(HttpServletRequest request) {
         LicenseBean licenseBean =
             (LicenseBean) request.getSession().getAttribute("licenseBean");
         if (licenseBean == null) {
             licenseBean = new LicenseBean();
             request.getSession().setAttribute("licenseBean", licenseBean);
         }
         return licenseBean;
     }
 
     public static LexEVSUtils.CSchemes getUnacceptedLicenses(
         HttpServletRequest request, List<String> ontologiesToSearchOn) {
         LicenseBean licenseBean = getLicenseBean(request);
         LexEVSUtils.CSchemes cSchemes = new LexEVSUtils.CSchemes();
         for (int i = 0; i < ontologiesToSearchOn.size(); i++) {
             String key = (String) ontologiesToSearchOn.get(i);
             String scheme = DataUtils.key2CodingSchemeName(key);
             String version = DataUtils.key2CodingSchemeVersion(key);
             boolean isLicensed = LicenseBean.isLicensed(scheme, version);
             boolean accepted = licenseBean.licenseAgreementAccepted(scheme);
             if (isLicensed && !accepted)
                 cSchemes.add(scheme, version);
         }
         return cSchemes;
     }
 
     public static void acceptLicense(HttpServletRequest request, String scheme) {
         LicenseBean licenseBean = getLicenseBean(request);
         licenseBean.addLicenseAgreement(scheme);
     }
 
     public static void acceptLicenses(HttpServletRequest request,
         LexEVSUtils.CSchemes schemes) {
         LicenseBean licenseBean = getLicenseBean(request);
         Iterator<LexEVSUtils.CScheme> iterator = schemes.iterator();
         while (iterator.hasNext()) {
             LexEVSUtils.CScheme cScheme = iterator.next();
             licenseBean.addLicenseAgreement(cScheme.getCodingScheme());
         }
     }
 
     public static boolean isLicensedAndNotAccepted(HttpServletRequest request,
         String scheme, String version) {
         boolean isLicensed = LicenseBean.isLicensed(scheme, version);
         if (! isLicensed)
             return false;
 
         LicenseBean licenseBean =
             (LicenseBean) request.getSession().getAttribute("licenseBean");
         if (licenseBean == null) {
             licenseBean = new LicenseBean();
             request.getSession().setAttribute("licenseBean", licenseBean);
         }
         boolean accepted = licenseBean.licenseAgreementAccepted(scheme);
         boolean value = isLicensed && !accepted;
         return value;
     }
 
     public static void clearAllLicenses(HttpServletRequest request) {
         _logger.debug(Utils.SEPARATOR);
         _logger.debug("Clear all licenses");
         LicenseBean licenseBean = getLicenseBean(request);
         licenseBean.clearAllLicenseAgreements();
     }
 
     public static class WebPageHelper {
         private LexEVSUtils.CSchemes _schemes = null;
 
         public WebPageHelper(LexEVSUtils.CSchemes schemes) {
             _schemes = schemes;
         }
 
         public WebPageHelper(String scheme, String version) {
             _schemes = new LexEVSUtils.CSchemes();
             _schemes.add(new LexEVSUtils.CScheme(scheme, version));
         }
 
 /*
         public String getReviewAndAcceptMessage() {
             StringBuffer buffer = new StringBuffer();
             buffer.append("To access <b>");
             Vector<String> list = _schemes.getCodingSchemes();
             list = Utils.unique(list);
 
             int n = list.size();
             for (int i = 0; i < n; ++i) {
                 String scheme = list.get(i);
                 if (n > 1 && i == (n - 1))
                     buffer.append(" and ");
                 else if (i > 0)
                     buffer.append(", ");
                 buffer.append(DataUtils
                     .getMetadataValue(scheme, "display_name"));
             }
             buffer.append("</b>, please review and accept the ");
             buffer.append("copyright/license statement below:");
             return buffer.toString();
         }
 */
 
 
         public String getReviewAndAcceptMessage() {
             StringBuffer buffer = new StringBuffer();
             if (_schemes == null) {
				buffer.append("No terminology is selected. Please press <b>Cancel</b> to return to the home page.");
 
 			} else {
 
 				buffer.append("To access <b>");
 				Vector<String> list = _schemes.getCodingSchemes();
 				list = Utils.unique(list);
 
 				int n = list.size();
 				for (int i = 0; i < n; ++i) {
 					String scheme = list.get(i);
 					if (n > 1 && i == (n - 1))
 						buffer.append(" and ");
 					else if (i > 0)
 						buffer.append(", ");
 					buffer.append(DataUtils
 						.getMetadataValue(scheme, "display_name"));
 				}
 				buffer.append("</b>, please review and accept the ");
 				buffer.append("copyright/license statement below:");
 
 		    }
             return buffer.toString();
         }
 
 
         public String getLicenseMessages(int maxChars) {
 			if (_schemes == null) return "";
 
             StringBuffer buffer = new StringBuffer();
             HashSet<String> hset = new HashSet<String>();
 
             int n = _schemes.size();
             for (int i = 0; i < n; ++i) {
                 LexEVSUtils.CScheme cScheme = _schemes.get(i);
                 String scheme = cScheme.getCodingScheme();
                 String version = cScheme.getVersion();
                 if (hset.contains(scheme))
                     continue;
                 if (i > 0)
                     buffer.append("\n");
                 if (n > 1) {
                     String name = cScheme.getDisplayName();
                     String separator =
                         Utils.fill("== " + name + " License: ", '=', maxChars);
                     buffer.append(separator + "\n");
                 }
                 buffer.append(LicenseBean.resolveCodingSchemeCopyright(scheme,
                     version).trim());
                 buffer.append("\n");
                 hset.add(cScheme.getCodingScheme());
             }
             return buffer.toString();
         }
 
         public String getButtonMessage() {
 			if (_schemes == null) return "";
             return "If and only if you agree to these terms and conditions,"
                 + " click the Accept button to proceed.";
         }
     }
 }
