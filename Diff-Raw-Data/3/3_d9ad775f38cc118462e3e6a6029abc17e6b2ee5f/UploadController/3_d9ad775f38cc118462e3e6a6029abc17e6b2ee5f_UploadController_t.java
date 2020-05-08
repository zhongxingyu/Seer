 package net.hmrradio.podcastsite.controller.audioFile;
 
 import net.hmrradio.podcastsite.controller.BaseController;
 import net.hmrradio.podcastsite.define.AttrName;
 import net.hmrradio.podcastsite.meta.AudioFileMeta;
 import net.hmrradio.podcastsite.model.AudioFile;
 import net.hmrradio.podcastsite.service.AudioFileService;
 
 import org.slim3.controller.Navigation;
 import org.slim3.controller.validator.LongTypeValidator;
 import org.slim3.controller.validator.RegexpValidator;
 import org.slim3.controller.validator.RequiredValidator;
 import org.slim3.controller.validator.Validators;
 import org.slim3.util.ApplicationMessage;
 import org.slim3.util.BeanUtil;
 
 public class UploadController extends BaseController {
 
     private AudioFileService audioFileService = new AudioFileService();
     private AudioFileMeta a = AudioFileMeta.get();
 
     public UploadController() {
         necessaryLoggedIn = true;
     }
 
     @Override
     protected Navigation exec() throws Exception {
 
         AudioFile audioFile = new AudioFile();
         BeanUtil.copy(request, audioFile);
 
         audioFileService.create(audioFile);
 
         requestScope("url", audioFile.getUrl());
         return forward("complete.jsp");
     }
 
     @Override
     protected boolean validate() {
         Validators v = new Validators(request);
 
         v.add(a.url, RequiredValidator.INSTANCE);
        v.add(a.duration, RequiredValidator.INSTANCE);
         v.add(a.length, RequiredValidator.INSTANCE, new LongTypeValidator());
         v.add(a.type, RequiredValidator.INSTANCE);
 
         return v.validate();
     }
 
     @Override
     protected Navigation input() throws Exception {
         requestScope(AttrName.ERROR_MESSAGES, errors.values());
         return forward("/error.jsp");
     }
 
     @Override
     protected Navigation exceptionError(String error) throws Exception {
         errors.put("global", ApplicationMessage.get(error));
         return forward("/error.jsp");
     }
 }
