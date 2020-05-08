 package controllers.site;
 
 import controllers.SecuredController;
import org.apache.commons.io.FileUtils;
 import play.Logger;
 import play.data.FileUpload;
 import play.mvc.Before;
 import play.mvc.results.NotFound;
 import tracker.Config;
 import tracker.DatabaseFactory;
 import tracker.bencode.Decoder;
 import tracker.bencode.Encoder;
 import tracker.cache.TorrentsCache;
 import tracker.pagination.CommentsPage;
 import tracker.pagination.PageNavigation;
 import tracker.torrents.Comment;
 import tracker.torrents.TorrentContentsResponse;
 import tracker.torrents.TorrentMetaInfo;
 import tracker.torrents.upload.TorrentApplyRequest;
 import tracker.torrents.upload.TorrentApplyResponse;
 import tracker.torrents.upload.TorrentUpload;
 import tracker.torrents.upload.TorrentUploadResponse;
 import tracker.util.Utils;
 
 import java.io.*;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 public class Torrent extends SecuredController {
 	@Before(unless = { "view" })
 	protected static void checkAuthentification() throws Throwable {
 		SecuredController.checkAuthentification();
 	}
 
     public static void view(Long torrentId, Integer page) throws Throwable {
 		tracker.torrents.Torrent torrent = TorrentsCache.getInstance().getById(torrentId);
 
 		if (torrent == null) {
 			throw new NotFound("");
 		}
 
 		if (page == null) {
 			page = 1;
 		}
 
 		CommentsPage commentsPage = torrent.getComments(page);
 		PageNavigation commentsPageNavigation = new PageNavigation(commentsPage.getPageSet(), page,
 				new String[] { torrentId.toString() });
 
 		// Обновление счетчика.
 		torrent.addView();
 
 		render(torrent, commentsPage, commentsPageNavigation);
 	}
 
 	public static void download(Long torrentId) throws Throwable {
 		tracker.torrents.Torrent torrent = TorrentsCache.getInstance().getById(torrentId);
 		if (torrent == null) {
 			notFound("Торрента с таким ID не существует.");
 			return;
 		}
 
 		// Загрузка базового торрент файла.
 		BufferedInputStream sourceStream = new BufferedInputStream(new FileInputStream(torrent.getTorrentFile()));
 
 		Map<String, Object> torrentMap = Decoder.get().decode(sourceStream);
 
 		// Подготовка торрента.
 		List<List<String>> announceUrls = new ArrayList<List<String>>();
 		for (Object item : Config.getList("tracker.announce.announcers")) {
 			String announceUrl = (String)item;
 			announceUrl += "?passkey=" + currentAccount.getPasskey();
 
 			List<String> tier = new ArrayList<String>();
 			tier.add(announceUrl);
 			announceUrls.add(tier);
 		}
 		for (Object item : Config.getList("tracker.announce.retrackers")) {
 			String announceUrl = (String)item;
 
 			List<String> tier = new ArrayList<String>();
 			tier.add(announceUrl);
 			announceUrls.add(tier);
 		}
 
 //		if (torrentMap.containsKey("announce")) {
 //			Logger.info(new String((byte[])torrentMap.get("announce"), Charset.forName(Config.getString("tracker.charset"))));
 //		}
 
 		torrentMap.put("announce", announceUrls.get(0).get(0));
 		torrentMap.put("announce-list", announceUrls);
 		torrentMap.put("comment", Config.getString("site.baseUrl") + "/torrent/" + torrent.getId());
 
 		// Обновление счетчика.
 		torrent.addHit();
 
 		// Вывод торрента.
 		InputStream finalStream = new ByteArrayInputStream(Encoder.get().encode(torrentMap).toByteArray());
 		String responseFileName = Config.getString("tracker.torrentFileName").replace("%id%", torrent.getId().toString());
 		renderBinary(finalStream, responseFileName, "application/x-bittorrent", false);
 	}
 
 	public static void upload() {
 		render();
 	}
 
 	public static void uploadApply(TorrentApplyRequest applyRequest) throws Throwable {
 		TorrentApplyResponse applyResponse = new TorrentApplyResponse();
 		File temporaryTorrentFile = null;
 		try {
 			// TODO: проверка title и description
 
 
 			TorrentUpload torrentUpload = TorrentUpload.byId(applyRequest.uploadId);
 
 			if (torrentUpload == null) {
 				applyResponse.setError("Неправильный идентификатор загруженного торрента.");
 				applyResponse.send();
 				return;
 			}
 
			String fileName = Config.getString("uploads.paths.torrent") + "/temp/" + torrentUpload.getId();
 			temporaryTorrentFile = new File(fileName);
 
 			torrentUpload.delete();
 
 			TorrentMetaInfo torrentMetaInfo = new TorrentMetaInfo(temporaryTorrentFile);
 			if (!Arrays.equals(torrentMetaInfo.getInfoHash(), torrentUpload.getInfoHash())) {
 				throw new Exception("Загруженный торрент имеет некорректный infoHash.");
 			}
 
 			tracker.torrents.Torrent torrent = tracker.torrents.Torrent.byInfoHash(torrentMetaInfo.getInfoHash());
 			if (torrent != null) {
 				applyResponse.setError("Такой торрент уже загружен на сервер");
 				applyResponse.send();
 				return;
 			}
 
			File torrentFile  = new File(Config.getString("uploads.paths.torrent"), torrentMetaInfo.getInfoHashHexString());
			if (torrentFile.exists()) {
				torrentFile.delete();
			}

			FileUtils.moveFile(temporaryTorrentFile, torrentFile);

 			torrent = new tracker.torrents.Torrent();
 			torrent.setAuthorId(currentAccount.getId());
 			torrent.setTitle(applyRequest.title);
 			torrent.setDescription(applyRequest.description);
 			torrent.setEditDate(Utils.getCurrentTimestamp());
 			torrent.setCreationDate(Utils.getCurrentTimestamp());
 			torrent.setInfoHash(torrentMetaInfo.getInfoHash());
 			torrent.setTorrentSize(torrentMetaInfo.getSize());
 			torrent.save();
 
 			TorrentsCache.getInstance().put(torrent);
 
 			applyResponse.setTorrentId(torrent.getId());
 			applyResponse.setTorrentTitle(torrent.getTitle());
 		} catch (Exception exception) {
 			Logger.error(exception, "");
			applyResponse.setError("Произошла ошибка при загрузке торрента.");
 			applyResponse.send();
 			return;
 		} finally {
 			// TODO: fixme
 //			if (temporaryTorrentFile != null && !temporaryTorrentFile.delete()) {
 //				Logger.error("Не удалось удалить временный файл торрента. " + temporaryTorrentFile.getAbsolutePath());
 //			}
 		}
 
 		applyResponse.send();
 	}
 
 	public static void uploadTorrentFile(FileUpload torrentFileUpload) throws Throwable {
 		TorrentUploadResponse uploadResponse = new TorrentUploadResponse();
 
 		if (torrentFileUpload == null) {
 			uploadResponse.setError("Не удалось загрузить торрент. Попробуйте снова.");
 			uploadResponse.send();
 			return;
 		}
 
 		try {
 			TorrentMetaInfo torrentMetaInfo = new TorrentMetaInfo(torrentFileUpload.asBytes());
 			uploadResponse.setInfoHash(torrentMetaInfo.getInfoHashHexString());
 
 			tracker.torrents.Torrent torrent = tracker.torrents.Torrent.byInfoHash(torrentMetaInfo.getInfoHash());
 			if (torrent != null) {
 				uploadResponse.setError("Такой торрент уже загружен на сервер.");
 				uploadResponse.send();
 				return;
 			}
 
 			Long uploadId = null;
 			Connection connection = null;
 			try {
 				connection = DatabaseFactory.getInstance().getConnection();
 
 				PreparedStatement statement = connection.prepareStatement("INSERT INTO torrent_uploads" +
 					"(`account_id`, `upload_date`, `info_hash`) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
 
 //				PreparedStatement statement = connection.prepareStatement("INSERT INTO torrent_uploads(`account_id`, `upload_date`, `info_hash`) " +
 //					"VALUES(?, ?) ON DUPLICATE KEY UPDATE " +
 //					"`account_id` = VALUES(`account_id`), `upload_date` = VALUES(`upload_date`), `info_hash` = VALUES(`info_hash`)",
 //					Statement.RETURN_GENERATED_KEYS);
 
 				statement.setLong(1, currentAccount.getId());
 				statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
 				statement.setBytes(3, torrentMetaInfo.getInfoHash());
 				statement.execute();
 
 				ResultSet keys = statement.getGeneratedKeys();
 				if (keys.next()) {
 					uploadId = keys.getLong(1);
 				}
 
 				keys.close();
 				statement.close();
 			} finally {
 				DatabaseFactory.close(connection);
 			}
 
 			uploadResponse.setUploadId(uploadId);
 
 			// Save torrent file
 			String uploadDirectory = Config.getString("uploads.paths.torrent");
 			File torrentFile = new File(uploadDirectory + "/temp", uploadId.toString());
 
 			if (torrentFile.exists()) {
 				if (!torrentFile.delete()) {
 					Logger.error("Не удалось удалить старый файл " + torrentFile.getAbsolutePath());
 				}
 			}
 
 			torrentFileUpload.asFile(torrentFile);
 		} catch (Exception exception) {
 			Logger.error(exception, "");
 			uploadResponse.setError("Произошла неизвестная ошибка при загрузке торрента.");
 			uploadResponse.send();
 			return;
 		}
 
 		uploadResponse.send();
 	}
 
 	public static void delete(Long torrentId) throws Throwable {
 		tracker.torrents.Torrent torrent = tracker.torrents.Torrent.byId(torrentId);
 		notFoundIfNull(torrent, "Такого торрента не существует.");
 		torrent.delete();
 	}
 
 	public static void postComment(CommentData commentData) throws Throwable {
 		tracker.torrents.Torrent torrent = TorrentsCache.getInstance().getById(commentData.torrentId);
 		notFoundIfNull(torrent, "Такого торрента не существует.");
 
 		Comment comment = new Comment();
 		comment.setTorrentId(torrent.getId());
 		comment.setAccountId(currentAccount.getId());
 		comment.setContent(commentData.content); // TODO: filter me!
 		comment.setVisible(true);
 		comment.setPostDate(new Timestamp(System.currentTimeMillis()));
 		comment.save();
 
 		Logger.info("Comment saved with ID=" + comment.getId());
 
 		view(torrent.getId(), torrent.getCommentsNextPage());
 	}
 
 	public static void getTorrentContents(Long torrentId) throws Throwable {
 		TorrentContentsResponse contentsResponse = new TorrentContentsResponse();
 
 		tracker.torrents.Torrent torrent = tracker.torrents.Torrent.byId(torrentId);
 		if (torrent == null) {
 			contentsResponse.setError("Такого торрента не существует.");
 			contentsResponse.send();
 			return;
 		}
 
 		File torrentFile = new File(Config.getString("uploads.paths.torrent"), torrent.getInfoHashHexString());
 		if (!torrentFile.exists()) {
 			contentsResponse.setError("Такого торрента не существует.");
 			contentsResponse.send();
 			return;
 		}
 
 		TorrentMetaInfo torrentMetaInfo = new TorrentMetaInfo(torrentFile);
 
 		List<TorrentContentsResponse.Entry> fileEntries = new ArrayList<TorrentContentsResponse.Entry>();
 		for (TorrentMetaInfo.FileMetaInfo fileMetaInfo : torrentMetaInfo.getFiles()) {
 			TorrentContentsResponse.Entry entry = new TorrentContentsResponse.Entry(fileMetaInfo.getPath(), fileMetaInfo.getFormattedSize());
 			fileEntries.add(entry);
 		}
 
 		contentsResponse.setFiles(fileEntries);
 		contentsResponse.send();
 	}
 
 	public static void getTemporaryTorrentContents(Long uploadId) throws Throwable {
 		TorrentContentsResponse contentsResponse = new TorrentContentsResponse();
 
 		File torrentFile = new File(Config.getString("uploads.paths.torrent") + "/temp", uploadId.toString());
 		if (!torrentFile.exists()) {
 			contentsResponse.setError("Такого торрента не существует.");
 			contentsResponse.send();
 			return;
 		}
 
 		TorrentMetaInfo torrentMetaInfo = new TorrentMetaInfo(torrentFile);
 
 		List<TorrentContentsResponse.Entry> fileEntries = new ArrayList<TorrentContentsResponse.Entry>();
 		for (TorrentMetaInfo.FileMetaInfo fileMetaInfo : torrentMetaInfo.getFiles()) {
 			TorrentContentsResponse.Entry entry = new TorrentContentsResponse.Entry(fileMetaInfo.getPath(), fileMetaInfo.getFormattedSize());
 			fileEntries.add(entry);
 		}
 
 		contentsResponse.setFiles(fileEntries);
 		contentsResponse.send();
 	}
 
 	public class CommentData {
 		public Long torrentId;
 		public String content;
 	}
 }
