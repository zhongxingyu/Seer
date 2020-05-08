 package org.motechproject.carereporting.service.impl;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.SystemUtils;
 import org.motechproject.carereporting.context.ServletContextProvider;
 import org.motechproject.carereporting.dao.LanguageDao;
 import org.motechproject.carereporting.domain.LanguageEntity;
 import org.motechproject.carereporting.domain.dto.MessageDto;
 import org.motechproject.carereporting.exception.CareMessageFileNotFoundRuntimeException;
 import org.motechproject.carereporting.exception.CareRuntimeException;
 import org.motechproject.carereporting.service.LanguageService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.StreamUtils;
 import org.springframework.web.context.support.ServletContextResource;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardOpenOption;
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 @Service
 @Transactional(readOnly = true)
 public class LanguageServiceImpl implements LanguageService {
 
     private static final String INVALID_CODE = "Invalid language code specified.";
     private static final String CARE_MESSAGE_DIRECTORY = SystemUtils.getUserHome().getAbsolutePath()
             + File.separatorChar + ".care" + File.separatorChar + "messages" + File.separatorChar;
     private static final String FILE_NAME = "messages_%s.properties";
 
     @Autowired
     private LanguageDao languageDao;
 
     @Autowired
     private ServletContextProvider servletContextProvider;
 
     @Override
     public Set<LanguageEntity> getAllLanguages() {
         return languageDao.getAll();
     }
 
     @Override
     public Set<LanguageEntity> getAllDefinedLanguages() {
         return languageDao.getAllWithDefined(true);
     }
 
     @Override
     public Set<LanguageEntity> getAllUndefinedLanguages() {
         return languageDao.getAllWithDefined(false);
     }
 
     @Override
     public LanguageEntity getLanguageByCode(String languageCode) {
         return languageDao.getLanguageByCode(languageCode);
     }
 
     @Override
     public Set<MessageDto> getMessagesForLanguage(String languageCode) {
         return getMessagesFromFile(languageCode);
     }
 
     @Override
     public String getMessagesForLanguagePlain(String languageCode) {
         return getMessagesFromFilePlain(languageCode);
     }
 
     @Override
     @Transactional(readOnly = false)
     public void defineLanguage(LanguageEntity languageEntity) {
         languageDao.update(languageEntity);
 
         this.setMessagesForLanguage(languageEntity);
     }
 
     @Override
     @Transactional(readOnly = false)
     public void updateLanguage(LanguageEntity languageEntity) {
         languageDao.update(languageEntity);
 
         this.setMessagesForLanguage(languageEntity);
     }
 
     @Override
     @Transactional(readOnly = false)
     public void removeLanguage(String languageCode) {
         String code = extractLanguageCode(languageCode);
         if (StringUtils.isBlank(code)) {
             throw new CareRuntimeException(INVALID_CODE);
         }
 
         LanguageEntity languageEntity = languageDao.getLanguageByCode(code);
         languageEntity.setDefined(false);
         languageDao.update(languageEntity);
 
         removeMessageFileForLanguage(languageEntity);
     }
 
     private void removeMessageFileForLanguage(LanguageEntity languageEntity) {
         try {
             if (StringUtils.isBlank(languageEntity.getCode())) {
                 throw new CareRuntimeException(INVALID_CODE);
             }
 
             Path filePath = Paths.get(String.format(CARE_MESSAGE_DIRECTORY + FILE_NAME, languageEntity.getCode()));
 
             Files.deleteIfExists(filePath);
         } catch (IOException e) {
             throw new CareRuntimeException(e);
         }
     }
 
     private Set<MessageDto> getMessagesFromFile(String languageCode) {
         try {
             String code = extractLanguageCode(languageCode);
             String fileName = String.format(CARE_MESSAGE_DIRECTORY + FILE_NAME, code);
 
             if (StringUtils.isBlank(code)) {
                return getDefaultMessageFileContentsAsJson();
             }
 
             File messageFile = new File(fileName);
             if (!messageFile.exists()) {
                 throw new CareMessageFileNotFoundRuntimeException(messageFile.getName());
             }
 
             Set<MessageDto> messages = new LinkedHashSet<>();
             List<String> lines = Files.readAllLines(
                     Paths.get(messageFile.getAbsolutePath()),
                     Charset.defaultCharset());
 
             for (String line : lines) {
                 if (!validateMessageLine(line)) {
                     continue;
                 }
 
                 String[] lineParts = line.split("=");
                 if (!validateMessageLineParts(lineParts)) {
                     continue;
                 }
 
                 messages.add(new MessageDto(lineParts[0], lineParts[1]));
             }
 
             return messages;
 
         } catch (IOException e) {
             throw new CareRuntimeException(e);
         }
     }
 
     private String getMessagesFromFilePlain(String languageCode) {
         try {
             String code = extractLanguageCode(languageCode);
             String fileName = String.format(CARE_MESSAGE_DIRECTORY + FILE_NAME, code);
 
             if (StringUtils.isBlank(code)) {
                return getDefaultMessageFileContents();
             }
 
             File messageFile = new File(fileName);
             if (!messageFile.exists()) {
                 throw new CareMessageFileNotFoundRuntimeException(messageFile.getName());
             }
 
            return StringUtils.join(Files.readAllLines(
                    Paths.get(messageFile.getAbsolutePath()),
                    Charset.defaultCharset()), '\n');
 
         } catch (IOException e) {
             throw new CareRuntimeException(e);
         }
     }
 
     private Set<MessageDto> getDefaultMessageFileContentsAsJson() {
         ServletContextResource resource = new ServletContextResource(
                 servletContextProvider.getServletContext(), "messages/messages.properties");
 
         try (InputStream inputStream = resource.getInputStream()) {
             Set<MessageDto> messages = new LinkedHashSet<>();
 
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
             while (bufferedReader.ready()) {
                 String line = bufferedReader.readLine();
                 if (!validateMessageLine(line)) {
                     continue;
                 }
 
                 String[] lineParts = line.split("=");
                 if (!validateMessageLineParts(lineParts)) {
                     continue;
                 }
 
                 messages.add(new MessageDto(lineParts[0], lineParts[1]));
             }
 
             return messages;
         } catch (IOException e) {
             throw new CareRuntimeException(e);
         }
     }
 
     private Boolean validateMessageLine(String messageLine) {
         if (StringUtils.isBlank(messageLine.trim()) || messageLine.startsWith("#")) {
             return false;
         }
 
         return true;
     }
 
     private Boolean validateMessageLineParts(String[] messageLineParts) {
         if (messageLineParts.length < 2 || StringUtils.isBlank(messageLineParts[0])
                 || StringUtils.isBlank(messageLineParts[1])) {
             return false;
         }
 
         return true;
     }
 
    private String getDefaultMessageFileContents() {
         ServletContextResource resource = new ServletContextResource(
                 servletContextProvider.getServletContext(), "messages/messages.properties");
 
         try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, Charset.defaultCharset());
         } catch (IOException e) {
             throw new CareRuntimeException(e);
         }
     }
 
     @Override
     public void setMessagesForLanguage(LanguageEntity languageEntity) {
         try {
             if (StringUtils.isBlank(languageEntity.getCode())) {
                 throw new CareRuntimeException(INVALID_CODE);
             }
 
             List<String> messageList = new ArrayList<>();
             for (MessageDto message : languageEntity.getMessages()) {
                 if (message.getCode() == null || message.getValue() == null) {
                     throw new CareRuntimeException(String.format("Cannot set messages for language %s.",
                             languageEntity.getName()));
                 }
 
                 messageList.add(message.toPropertyString());
             }
 
             String fileName = String.format(CARE_MESSAGE_DIRECTORY + FILE_NAME, languageEntity.getCode());
             File messageDir = new File(CARE_MESSAGE_DIRECTORY);
             File messageFile = new File(fileName);
 
             if (!messageDir.exists()) {
                 messageDir.mkdirs();
             }
 
             Files.write(
                     Paths.get(messageFile.getAbsolutePath()),
                     messageList,
                     Charset.defaultCharset(),
                     StandardOpenOption.CREATE,
                     StandardOpenOption.TRUNCATE_EXISTING,
                     StandardOpenOption.WRITE);
         } catch (IOException e) {
             throw new CareRuntimeException(e);
         }
     }
 
     private String extractLanguageCode(String from) {
         if (StringUtils.isBlank(from)) {
             return null;
         }
 
         return from.replaceAll("(messages(_)?)|([.]properties)", "").replaceAll("-", "_").trim();
     }
 
 }
