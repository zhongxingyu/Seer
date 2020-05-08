 package ru.terra.twochsaver.twoch;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ru.terra.jbrss.model.Model;
 import ru.terra.jbrss.util.RequestUtil;
 import ru.terra.twochsaver.twoch.db.TMessage;
 import ru.terra.twochsaver.twoch.db.TThread;
 import ru.terra.twochsaver.twoch.db.controller.TMessageJpaController;
 import ru.terra.twochsaver.twoch.db.controller.TThreadJpaController;
 import ru.terra.twochsaver.twoch.db.controller.exceptions.PreexistingEntityException;
 import ru.terra.twochsaver.twoch.dto.PostDTO;
 import ru.terra.twochsaver.twoch.dto.SingleThreadDTO;
 import flexjson.JSONDeserializer;
 
 public class SingleThreadDownloader {
 	private TMessageJpaController mpm;
 
 	@Inject
 	private Model model;
 
 	private final static String JSON = ".json";
 
 	Logger log = LoggerFactory.getLogger(SingleThreadDownloader.class);
 	private ExecutorService service;
 	private TThreadJpaController tpm;
 
 	public SingleThreadDownloader() {
 		EntityManagerFactory emf = Persistence.createEntityManagerFactory("2chPU");
 		mpm = new TMessageJpaController(emf);
 		tpm = new TThreadJpaController(emf);
 		service = Executors.newFixedThreadPool(5);
 	}
 
 	public Integer download(String url) throws Exception {
 		url = url.substring(0, url.lastIndexOf("."));
 		URLConnection conn = new URL(url + JSON).openConnection();
 		String json = RequestUtil.readStreamToString(conn.getInputStream(), "UTF-8");
 		SingleThreadDTO tdto = new JSONDeserializer<SingleThreadDTO>().deserialize(json, SingleThreadDTO.class);
 		PostDTO firstMsg = null;
 		Integer ret = 0;
 		for (List<PostDTO> msgsInThread : tdto.thread) {
 			ret += 1;
 			PostDTO msg = msgsInThread.get(0);
 			if (firstMsg == null)
 				firstMsg = msg;
 			if (!mpm.isMessageExists(msg.num)) {
 				log.info("Message " + msg.num + " cached");
 				mpm.create(new TMessage(msg));
 				if (msg.image != null) {
 					service.submit(new DownloaderThread(url + msg.image, firstMsg.num.toString()));
 				}
 			}
 		}
 		TThread t = tpm.findByStartMessage(firstMsg.num);
 		if (t == null) {
 			t = new TThread(0L, firstMsg.num, url);
 			tpm.create(t);
 		}
 		return ret;
 	}
 
 	public String loadName(String url) throws Exception {
 		url = url.substring(0, url.lastIndexOf("."));
 		URLConnection conn = new URL(url + JSON).openConnection();
 		String json = RequestUtil.readStreamToString(conn.getInputStream(), "UTF-8");
 		SingleThreadDTO tdto = new JSONDeserializer<SingleThreadDTO>().deserialize(json, SingleThreadDTO.class);
		return tdto.thread.get(0).get(0).name;
 	}
 }
