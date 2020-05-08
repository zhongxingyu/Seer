 package com.subsconvertor.webapp;
 
 import com.google.api.translate.Language;
 import com.subsconvertor.ConversionExec;
 import com.subsconvertor.dao.SubtitleDao;
 import com.subsconvertor.dao.jpa.JpaSubtitleDao;
 import com.subsconvertor.detector.SubsDetector;
 import com.subsconvertor.exception.SubtitleTypeException;
 import com.subsconvertor.model.Subtitle;
 import com.subsconvertor.model.SubtitleType;
 import com.subsconvertor.utils.FileUtils;
 import org.gmr.web.multipart.GMultipartFile;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 /*
 
  TODO: exception handling - we now catch all and print stack trace.  We should throw a runtime exception. and handle it in a generalized way (possibly a servlet filter or something from spring)
 
  TODO: also we should validate the inpot coming, especially the file type - contents, size, checksum (md5 or other checksums) to avpoid duplicates
 
  TODO: try to remove the nested ifs and long boolean conditions. Refactor this
 
  TODO: refactor to use better names (ex: sub amd subb)
 
  TODO: langauges can be static content
 
  TODO: add a logging library
 
 */
 
 
 @Controller
 @RequestMapping(value = {"/", "/synchronize", " * "})
 public class SubSynchController {
 
     private static final String LANGUAGES = "languages";
     private static final String MESSAGES = "messages";
     private static final String SYNCHRONIZE = "synchronize";
     private static final String NONE = "none";
 
     @Autowired
     private SubtitleDao subtitleDao;
 
     @Autowired
     private JpaSubtitleDao jpaDao;
 
     private final Language[] languages = Language.values();
 
 
     @RequestMapping(method = RequestMethod.GET)
     public ModelAndView getPage() {
         ModelAndView model = new ModelAndView();
         model.setViewName(SYNCHRONIZE);
         model.addObject(LANGUAGES, languages);
         return model;
     }
 
     @RequestMapping(value = "/synchronize", method = RequestMethod.POST)
     public ModelAndView saveAndConvertSub(@RequestParam("framerateFrom") float framerateFrom,
                                           @RequestParam("framerateInto") float framerateInto,
                                           @RequestParam("subtitle") GMultipartFile sub,
                                           @RequestParam("subtitleType") String subtitleTypeChosen,
                                           @RequestParam("languageFrom") String languageFrom,
                                           @RequestParam("languageInto") String languageInto,
                                           HttpServletResponse response) {
 
 
         List<String> messages = new ArrayList<String>();
 
         ModelAndView model = createModelAndView();
 
        if (sub == null || sub.isEmpty()) {
             messages.add("Please choose a subtitle!");
             model.addObject(MESSAGES, messages);
            return model;
         }
 
         // TODO: validate this
         final byte[] binatyContents = sub.getBytes();
         final String conetentType = sub.getContentType();
         final String originalFileName = sub.getOriginalFilename();
 
 
         SubsDetector detect = new SubsDetector();
         try {
             SubtitleType typeDetected = detect.detectSubtitleType(binatyContents);
             String subtitleExtension = typeDetected.getExtension();
 
             if ((framerateFrom != 0.0 && framerateInto != 0.0 &&
                     (framerateFrom != framerateInto ||
                             (framerateFrom == framerateInto && (!subtitleTypeChosen.equals(NONE) && !subtitleTypeChosen.equals(typeDetected.toString()))))) ||
                     (!languageFrom.equals(NONE) && !languageInto.equals(NONE) && !languageFrom.equals(languageInto))) {
 
                 Subtitle subb = new Subtitle();
                 subb.setSubtitleName(originalFileName);
                 subb.setSubtitleContentType(conetentType);
                 subb.setSubtitleOriginalContent(binatyContents);
 
                 ConversionExec conv = new ConversionExec(detect);
 
                 if (framerateFrom != 0.0 && framerateInto != 0.0 &&
                         (framerateFrom != framerateInto ||
                                (framerateFrom == framerateInto && (!subtitleTypeChosen.equals(NONE) && !subtitleTypeChosen.equals(typeDetected.toString()))))) {
                     subb.setFramerateFrom(framerateFrom);
                     subb.setFramerateInto(framerateInto);
                     conv.setFromFramerate(new BigDecimal(framerateFrom));
                     conv.setToFramerate(new BigDecimal(framerateInto));
                    if (!subtitleTypeChosen.equals(NONE) && !subtitleTypeChosen.equals(typeDetected.toString())) {
                         subb.setSubtitleType(subtitleTypeChosen);
                         conv.setSubtitleType(SubtitleType.valueOf(subtitleTypeChosen));
                         subtitleExtension = SubtitleType.valueOf(subtitleTypeChosen).getExtension();
                     } else {
                         subb.setSubtitleType(typeDetected.toString());
                     }
                 } else {
                     subb.setFramerateFrom(0);
                     subb.setFramerateInto(0);
                     conv.setFromFramerate(new BigDecimal(0));
                     conv.setToFramerate(new BigDecimal(0));
                     subb.setSubtitleType(typeDetected.toString());
                 }
 
                 if (!languageFrom.equals(NONE) && !languageInto.equals(NONE) && !languageFrom.equals(languageInto)) {
                     subb.setLanguage(languageInto);
                     conv.setLanguageFrom(Language.valueOf(languageFrom));
                     conv.setLanguageInto(Language.valueOf(languageInto));
                 }
 
                 StringBuilder convertedSub = null;
                 try {
                     convertedSub = conv.convert(binatyContents);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 subb.setSubtitleConvertedContent(convertedSub.toString().getBytes());
 
                 jpaDao.save(subb);
 
                 try {
                     response.setHeader("Content-Disposition", "inline;filename=\""
                             + FileUtils.getFileName(originalFileName) + "_subsynched"
                             + subtitleExtension
                             + "\"");
                     OutputStream out = response.getOutputStream();
                     response.setContentType(conetentType);
                     out.write(convertedSub.toString().getBytes());
                     out.flush();
                     out.close();
                     return null;
 
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             } else {
                 messages.add("Choose to change the framerate, subtitle type or subtitle language!");
             }
         } catch (SubtitleTypeException ex) {
             messages.add(ex.getMessage());
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         if (messages.size() > 0) {
             model.addObject(MESSAGES, messages);
         }
 
         return model;
     }
 
     private ModelAndView createModelAndView() {
         ModelAndView model = new ModelAndView();
         model.addObject(LANGUAGES, languages);
         model.setViewName(SYNCHRONIZE);
         return model;
     }
 
 }
