 /*****************************************************************************
  * Copyright  2011 , UT-Battelle, LLC All rights reserved
  *
  * OPEN SOURCE LICENSE
  *
  * Subject to the conditions of this License, UT-Battelle, LLC (the
  * Licensor) hereby grants to any person (the Licensee) obtaining a copy
  * of this software and associated documentation files (the "Software"), a
  * perpetual, worldwide, non-exclusive, irrevocable copyright license to use,
  * copy, modify, merge, publish, distribute, and/or sublicense copies of the
  * Software.
  *
  * 1. Redistributions of Software must retain the above open source license
  * grant, copyright and license notices, this list of conditions, and the
  * disclaimer listed below.  Changes or modifications to, or derivative works
  * of the Software must be noted with comments and the contributor and
  * organizations name.  If the Software is protected by a proprietary
  * trademark owned by Licensor or the Department of Energy, then derivative
  * works of the Software may not be distributed using the trademark without
  * the prior written approval of the trademark owner.
  *
  * 2. Neither the names of Licensor nor the Department of Energy may be used
  * to endorse or promote products derived from this Software without their
  * specific prior written permission.
  *
  * 3. The Software, with or without modification, must include the following
  * acknowledgment:
  *
  *    "This product includes software produced by UT-Battelle, LLC under
  *    Contract No. DE-AC05-00OR22725 with the Department of Energy.
  *
  * 4. Licensee is authorized to commercialize its derivative works of the
  * Software.  All derivative works of the Software must include paragraphs 1,
  * 2, and 3 above, and the DISCLAIMER below.
  *
  *
  * DISCLAIMER
  *
  * UT-Battelle, LLC AND THE GOVERNMENT MAKE NO REPRESENTATIONS AND DISCLAIM
  * ALL WARRANTIES, BOTH EXPRESSED AND IMPLIED.  THERE ARE NO EXPRESS OR
  * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE,
  * OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY PATENT, COPYRIGHT,
  * TRADEMARK, OR OTHER PROPRIETARY RIGHTS, OR THAT THE SOFTWARE WILL
  * ACCOMPLISH THE INTENDED RESULTS OR THAT THE SOFTWARE OR ITS USE WILL NOT
  * RESULT IN INJURY OR DAMAGE.  The user assumes responsibility for all
  * liabilities, penalties, fines, claims, causes of action, and costs and
  * expenses, caused by, resulting from or arising out of, in whole or in part
  * the use, storage or disposal of the SOFTWARE.
  *
  *
  ******************************************************************************/
 
 
 /**
  *
  * SearchSetting controller provides a front an interface to
  * manipulate search settings, the settings are then persisted to
  * a file.
  *
  * Note that we could piggyback these properties to the existing
  * /esg/config/esgf.properties, which should be a trivial change.
  *
  *
  * @author Feiyi Wang (fwang2@ornl.gov)
  *
  */
 package org.esgf.web;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.esgf.domain.SearchSetting;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
//@Controller
//@RequestMapping(value = "/setting/*")
 public class SearchSettingController {
     private final static Logger LOG = Logger.getLogger(SearchSettingController.class);
    private final static String ESGF_PROP_FILE = "classpath:esgf-fe.properties";
     private final Properties prop = new Properties();
     private File configFile = null;
 
     public SearchSettingController() throws IOException {
        configFile = null;
         //--------------------------------------------
 
         try {
             prop.load(new FileInputStream(configFile));
             LOG.debug("Found old property file");
         } catch (FileNotFoundException e) {
             LOG.debug("Create new property file");
             prop.setProperty("googleScholar", "false");
             prop.setProperty("mendeley", "false");
             prop.setProperty("annotate", "false");
             prop.store(new FileOutputStream(configFile), null);
         }
 
         LOG.debug("property file: " + configFile.getAbsolutePath());
     }
 
     public void saveSetting(SearchSetting setting) throws IOException {
         prop.setProperty("annotate", setting.getAnnotate().toString());
         prop.setProperty("mendeley", setting.getMendeley().toString());
         prop.setProperty("googleScholar", setting.getGoogleScholar().toString());
         prop.store(new FileOutputStream(configFile), null);
     }
 
     @ModelAttribute("setting")
     public SearchSetting loadSetting() {
 
         SearchSetting setting = new SearchSetting();
 
         if (prop.containsKey("annotate")) {
             setting.setAnnotate(prop.getProperty("annotate"));
         } else {
             setting.setAnnotate("false");
         }
 
         if (prop.containsKey("googleScholar")) {
             setting.setGoogleScholar(prop.getProperty("googleScholar"));
         } else {
             setting.setGoogleScholar("false");
         }
 
         if (prop.containsKey("mendeley")) {
             setting.setMendeley(prop.getProperty("mendeley"));
         } else {
             setting.setMendeley("false");
         }
 
         return setting;
 
     }
 
     @RequestMapping("show")
     public String display(Model model) {
         LOG.debug("setup form ... ");
         return "admin/setting";
 
     }
 
     @RequestMapping(value="save", method = RequestMethod.POST)
     public String processSubmit(
                 @ModelAttribute("setting") SearchSetting setting,
                 BindingResult results) throws IOException {
 
         // save java object to prop file format
         saveSetting(setting);
         LOG.debug("Setting Saved");
         return "admin/setting_saved";
     }
 
     @RequestMapping(value="queryAnnotate")
     public @ResponseBody String queryAnnotate() {
         LOG.debug("setting query received");
         return loadSetting().getAnnotate();
     }
 }
