 package ru.redsolution.rosyama.data;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import ru.redsolution.rosyama.R;
 import android.app.Activity;
 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 
 /**
  * Класс приложения.
  * 
  * @author alexander.ivanov
  * 
  */
 public class Rosyama extends Application implements UpdateListener,
 		LocationListener {
 	/**
 	 * Писать логи, использовать точку по умолчанию для геокодинга.
 	 */
 	static final boolean DEBUG = false;
 
 	/**
 	 * Формат имени файла для записи запросов и ответов.
 	 */
 	public final static SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat(
 			"yyyy-MM-dd-HH-mm-ss");
 
 	/**
 	 * Формат отображения даты.
 	 */
 	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
 			"dd MMMM yyyy");
 
 	/**
 	 * Ключ авторизации для геокодинга.
 	 */
 	private static final String YANDEX_KEY = "ACG3DU0BAAAA6wkJGgQAgRY2ChXSECRoRN9yDmLrz_wSRYQAAAAAAAAAAABC1YjXH-X5KWN_NyvBCtLgrvoZSg==";
 
 	/**
 	 * Геокодер.
 	 */
 	private static final String YANDEX_LOCATION = "http://geocode-maps.yandex.ru/1.x/?key=%s&geocode=%s&results=1";
 
 	/**
 	 * Обратный геокодер.
 	 */
 	private static final String YANDEX_ADDRESS = "http://geocode-maps.yandex.ru/1.x/?key=%s&geocode=%s%%2C%s&results=1";
 
 	/**
 	 * XML хост РосЯмы.
 	 */
 	public static final String XML_HOST;
 	public static final String WEB_HOST;
 
 	static {
 		if (DEBUG) {
 			XML_HOST = "http://xml-st1234.greensight.ru";
 			WEB_HOST = "http://st1234.greensight.ru";
 		} else {
 			XML_HOST = "http://xml.rosyama.ru";
 			WEB_HOST = "http://rosyama.ru";
 		}
 	}
 
 	/**
 	 * Адрес регистрации через веб.
 	 */
 	public static final String REGISTER_URL = WEB_HOST
 			+ "/personal/holes.php?register=yes";
 
 	/**
 	 * Размер изображения в предпросмотре.
 	 */
 	public static int IMAGE_PREVIEW_SIZE;
 
 	/**
 	 * Клиент для http соединения.
 	 */
 	private Client client;
 
 	/**
 	 * Системные настройки.
 	 */
 	private SharedPreferences settings;
 
 	/**
 	 * Слушатель изменения состояния.
 	 */
 	private UpdateListener updateListener;
 
 	/**
 	 * Список дефектов.
 	 */
 	private ArrayList<Hole> holes;
 
 	/**
 	 * Авторизация.
 	 */
 	private final AuthorizeOperation authorizeOperation;
 
 	/**
 	 * Отправка.
 	 */
 	private final SendOperation sendOperation;
 
 	/**
 	 * Удаление.
 	 */
 	private final DeleteOperation deleteOperation;
 
 	/**
 	 * Запрос списока.
 	 */
 	private final ListOperation listOperation;
 
 	/**
 	 * Глава ГИБДД.
 	 */
 	private final HeadOperation headOperation;
 
 	/**
 	 * PDF.
 	 */
 	private final PDFOperation pdfOperation;
 
 	/**
 	 * Получение адреса.
 	 */
 	private final AddressOperation addressOperation;
 
 	/**
 	 * Получение координат.
 	 */
 	private final LocationOperation locationOperation;
 
 	public Rosyama() {
 		client = new Client(this);
 		updateListener = null;
 		holes = new ArrayList<Hole>();
 		authorizeOperation = new AuthorizeOperation();
 		sendOperation = new SendOperation();
 		deleteOperation = new DeleteOperation();
 		listOperation = new ListOperation();
 		headOperation = new HeadOperation();
 		pdfOperation = new PDFOperation();
 		addressOperation = new AddressOperation();
 		locationOperation = new LocationOperation();
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		settings = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		IMAGE_PREVIEW_SIZE = getResources().getDimensionPixelSize(
 				R.dimen.image_preview_size);
 		if (!"".equals(getPassword()))
 			authorizeOperation.complited = true;
 	}
 
 	/**
 	 * Устанавливает случателя за изменениями данных. Должен вызвываться из
 	 * {@link Activity#onResume} и из {@link Activity#onPause}.
 	 * 
 	 * @param updateListener
 	 */
 	public void setUpdateListener(UpdateListener updateListener) {
 		this.updateListener = updateListener;
 		onUpdate();
 	}
 
 	/**
 	 * Возвращает дефект по его номеру.
 	 * 
 	 * @param id
 	 *            используйте <code>null</code> для нового дефекта
 	 * @return может вернуть <code>null</code>, если дефект не найден.
 	 */
 	public Hole getHole(String id) {
 		for (Hole hole : holes)
 			if ((id == null && hole.getId() == id)
 					|| (id != null && id.equals(hole.getId())))
 				return hole;
 		return null;
 	}
 
 	/**
 	 * Возвращает дефекты с указанным статусом.
 	 * 
 	 * @param status
 	 * @return
 	 */
 	public List<Hole> getHoles(Status status) {
 		ArrayList<Hole> result = new ArrayList<Hole>();
 		for (Hole hole : holes)
 			if (hole.getStatus() == status && hole.getId() != null)
 				result.add(hole);
 		return Collections.unmodifiableList(result);
 	}
 
 	/**
 	 * Создает не отправленный дефекта.
 	 * 
 	 * @param path
 	 *            Путь до фотографии.
 	 */
 	public void createHole(Uri path) {
 		// Получение гео данных
 		Location gps = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
 				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		Location net = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
 				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		Location location = net;
 		// Определение лучших данных
 		if (gps != null && net != null) {
 			if (gps.getTime() > net.getTime())
 				location = gps;
 			else if (net.hasAccuracy()) {
 				float[] results = new float[1];
 				Location.distanceBetween(net.getLatitude(), net.getLongitude(),
 						gps.getLatitude(), gps.getLongitude(), results);
 				if (results[0] < net.getAccuracy())
 					location = gps;
 			}
 		} else if (net == null)
 			location = gps;
 		// Удаление неотправленного дефекта.
 		Hole hole = getHole(null);
 		if (hole != null)
 			holes.remove(hole);
 		// Добавление нового дефекта.
 		holes.add(new Hole(this, path, location));
 	}
 
 	/**
 	 * Возвращает логин.
 	 * 
 	 * @return
 	 */
 	public String getLogin() {
 		return settings.getString(getString(R.string.login_key), "");
 	}
 
 	/**
 	 * Возвращает пароль.
 	 * 
 	 * @return
 	 */
 	public String getPassword() {
 		return settings.getString(getString(R.string.password_key), "");
 	}
 
 	/**
 	 * Возвращает собственное имя.
 	 * 
 	 * @return
 	 */
 	public String getFrom() {
 		return settings.getString(getString(R.string.from_key), "");
 
 	}
 
 	/**
 	 * Изменяет собственное имя.
 	 * 
 	 * @param from
 	 */
 	public void setFrom(String from) {
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString(getString(R.string.from_key), from);
 		editor.commit();
 		onUpdate();
 	}
 
 	/**
 	 * Возвращает собственную подпись.
 	 * 
 	 * @return
 	 */
 	public String getSignature() {
 		return settings.getString(getString(R.string.signature_key), "");
 	}
 
 	/**
 	 * Изменяет собственную подпись.
 	 * 
 	 * @param signature
 	 */
 	public void setSignature(String signature) {
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString(getString(R.string.signature_key), signature);
 		editor.commit();
 		onUpdate();
 	}
 
 	/**
 	 * Возвращает собственный почтовый адрес.
 	 * 
 	 * @return
 	 */
 	public String getPostAddress() {
 		return settings.getString(getString(R.string.postaddress_key), "");
 	}
 
 	/**
 	 * Изменяет собственный почтовый адрес.
 	 * 
 	 * @param postAddress
 	 */
 	public void setPostAddress(String postAddress) {
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString(getString(R.string.postaddress_key), postAddress);
 		editor.commit();
 		onUpdate();
 	}
 
 	public AuthorizeOperation getAuthorizeOperation() {
 		return authorizeOperation;
 	}
 
 	/**
 	 * Выйти и забыть пароль.
 	 */
 	public void logout() {
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString(getString(R.string.password_key), "");
 		editor.commit();
 		authorizeOperation.complited = false;
 		onUpdate();
 	}
 
 	public SendOperation getSendOperation() {
 		return sendOperation;
 	}
 
 	public DeleteOperation getDeleteOperation() {
 		return deleteOperation;
 	}
 
 	public ListOperation getListOperation() {
 		return listOperation;
 	}
 
 	public HeadOperation getHeadOperation() {
 		return headOperation;
 	}
 
 	public PDFOperation getPDFOperation() {
 		return pdfOperation;
 	}
 
 	public AddressOperation getAddressOperation() {
 		return addressOperation;
 	}
 
 	public LocationOperation getLocationOperation() {
 		return locationOperation;
 	}
 
 	/**
 	 * Авторизация.
 	 * 
 	 * @author alexander.ivanov
 	 * 
 	 */
 	public class AuthorizeOperation extends
 			AbstractOperation<String, LoginAndPassword> {
 
 		public AuthorizeOperation() {
 			super(Rosyama.this);
 		}
 
 		@Override
 		LoginAndPassword process(String... params) throws LocalizedException {
 			HashMap<String, String> form = new HashMap<String, String>();
 			form.put("login", params[0]);
 			form.put("password", params[1]);
 			client.getCheckedElement(client.post(Rosyama.XML_HOST
 					+ "/authorize/", form, null));
 			return new LoginAndPassword(params[0], params[1]);
 		}
 
 		@Override
 		void onSuccess(LoginAndPassword result) {
 			SharedPreferences.Editor editor = settings.edit();
 			editor.putString(getString(R.string.login_key), result.login);
 			editor.putString(getString(R.string.password_key), result.password);
 			editor.commit();
 		}
 
 		@Override
 		void onClear() {
 		}
 	}
 
 	/**
 	 * Создание, обновление дефекта.
 	 * 
 	 * @author alexander.ivanov
 	 * 
 	 */
 	public class SendOperation extends AbstractOperation<Hole, HoleAndHole> {
 		/**
 		 * Идентификатор отправленного дефекта.
 		 */
 		private Hole hole;
 
 		public SendOperation() {
 			super(Rosyama.this);
 			hole = null;
 		}
 
 		@Override
 		HoleAndHole process(Hole... params) throws LocalizedException {
 			Hole hole = params[0];
 			HashMap<String, String> form = new HashMap<String, String>();
 			form.put("login", getLogin());
 			form.put("password", getPassword());
 			form.put("address", hole.getAddress());
 			Location location = hole.getLocation();
 			if (location == null) {
 				location = new Location(LocationManager.GPS_PROVIDER);
 				location.setLatitude(55.693163);
 				location.setLongitude(37.707086);
 			}
 			form.put("latitude", getPoint(location.getLatitude()));
 			form.put("longitude", getPoint(location.getLongitude()));
 			form.put("comment", hole.getComment());
 			form.put("type", hole.getType().toString());
 			StringBuffer delete = null;
 			for (RemotePhoto remotePhoto : hole.getPhotosToRemove()) {
 				List<String> segments = remotePhoto.getUri().getPathSegments();
 				if (segments.isEmpty())
 					continue;
 				String value = segments.get(segments.size() - 1);
 				if (delete == null)
 					delete = new StringBuffer(value);
 				else
 					delete.append(",").append(value);
 			}
 			if (delete != null)
 				form.put("deletefiles", delete.toString());
 			HashMap<String, String> files = new HashMap<String, String>();
 			int index = 0;
 			for (LocalPhoto photo : hole.getPhotosToSend()) {
 				index++;
 				files.put("photo-" + index, photo.getUri().getPath());
 			}
 			String uri;
 			if (hole.getId() == null)
 				uri = XML_HOST + "/add/";
 			else
 				uri = String.format(XML_HOST + "/my/%s/update/", hole.getId());
 			Element element = client.getCheckedElement(client.post(uri, form,
 					files));
 			NodeList nodeList = element.getElementsByTagName("callresult");
 			if (nodeList.getLength() != 1)
 				throw new LocalizedException(R.string.hole_fail);
 			Element callresult = (Element) nodeList.item(0);
 			if (!"1".equals(callresult.getAttribute("result")))
 				throw new LocalizedException(R.string.hole_fail);
 			String id;
 			if (hole.getId() == null)
 				id = callresult.getAttribute("inserteddefectid");
 			else
 				id = hole.getId();
 			form.clear();
 			form.put("login", getLogin());
 			form.put("password", getPassword());
 			element = client.getCheckedElement(client.post(
 					String.format(XML_HOST + "/my/%s/", id), form, null));
 			for (Element holeElement : new ElementIterable(
 					element.getElementsByTagName("hole")))
 				return new HoleAndHole(hole, parseHole(holeElement));
 			throw new LocalizedException(R.string.data_fail);
 		}
 
 		@Override
 		void onClear() {
 			hole = null;
 		}
 
 		@Override
 		void onSuccess(HoleAndHole result) {
 			int index = holes.indexOf(result.source);
 			holes.remove(result.source);
 			holes.add(index, result.result);
 			hole = result.result;
 		}
 
 		/**
 		 * Возвращает номер опубликованного дефекта.
 		 * 
 		 * @return
 		 */
 		public String getId() {
 			return hole.getId();
 		}
 
 	}
 
 	/**
 	 * Удаление дефекта.
 	 * 
 	 * @author alexander.ivanov
 	 * 
 	 */
 	public class DeleteOperation extends AbstractOperation<Hole, Hole> {
 		public DeleteOperation() {
 			super(Rosyama.this);
 		}
 
 		@Override
 		Hole process(Hole... params) throws LocalizedException {
 			Hole hole = params[0];
 			HashMap<String, String> form = new HashMap<String, String>();
 			form.put("login", getLogin());
 			form.put("password", getPassword());
 			client.getCheckedElement(client.post(
 					String.format(XML_HOST + "/my/%s/delete/", hole.getId()),
 					form, null));
 			return hole;
 		}
 
 		@Override
 		void onClear() {
 		}
 
 		@Override
 		void onSuccess(Hole result) {
 			holes.remove(result);
 		}
 	}
 
 	/**
 	 * Запрос списка собственных дефектов.
 	 * 
 	 * @author alexander.ivanov
 	 * 
 	 */
 	public class ListOperation extends
 			AbstractOperation<Void, Collection<Hole>> {
 		public ListOperation() {
 			super(Rosyama.this);
 		}
 
 		@Override
 		Collection<Hole> process(Void... params) throws LocalizedException {
 			ArrayList<Hole> holes = new ArrayList<Hole>();
 			HashMap<String, String> form = new HashMap<String, String>();
 			form.put("login", getLogin());
 			form.put("password", getPassword());
 			Element element = client.getCheckedElement(client.post(XML_HOST
 					+ "/my/", form, null));
 			for (Element hole : new ElementIterable(
 					element.getElementsByTagName("hole")))
 				holes.add(parseHole(hole));
 			return holes;
 		}
 
 		@Override
 		void onClear() {
 		}
 
 		@Override
 		void onSuccess(Collection<Hole> result) {
 			holes.clear();
 			holes.addAll(result);
 		}
 	}
 
 	/**
 	 * Запрос главы ГИБДД.
 	 * 
 	 * @author alexander.ivanov
 	 * 
 	 */
 	public class HeadOperation extends AbstractOperation<Hole, HoleAndHead> {
 		public HeadOperation() {
 			super(Rosyama.this);
 		}
 
 		@Override
 		HoleAndHead process(Hole... params) throws LocalizedException {
 			Hole hole = params[0];
 			HashMap<String, String> form = new HashMap<String, String>();
 			form.put("login", getLogin());
 			form.put("password", getPassword());
 			Element element = client.getCheckedElement(client.post(
 					String.format(XML_HOST + "/my/%s/getgibddhead/",
 							hole.getId()), form, null));
 			NodeList nodeList = element.getElementsByTagName("gibddhead");
 			if (nodeList.getLength() != 1)
 				throw new LocalizedException(R.string.head_fail);
 			Element gibddhead = (Element) nodeList.item(0);
 			for (Element dative : new ElementIterable(
 					gibddhead.getElementsByTagName("dative"))) {
 				String post = dative.getAttribute("post");
 				String name = Client.getTextContent(dative);
 				if (!"".equals(post) && !"".equals(name))
 					return new HoleAndHead(hole, post + ", " + name);
 				else
 					return new HoleAndHead(hole, post + name);
 			}
 			throw new LocalizedException(R.string.head_fail);
 		}
 
 		@Override
 		void onClear() {
 		}
 
 		@Override
 		void onSuccess(HoleAndHead result) {
 			result.hole.setTo(result.head);
 		}
 	}
 
 	/**
 	 * Запрос заявления.
 	 * 
 	 * @author alexander.ivanov
 	 * 
 	 */
 	public class PDFOperation extends AbstractOperation<Hole, File> {
 		/**
 		 * Полученный файл.
 		 */
 		File file;
 
 		public PDFOperation() {
 			super(Rosyama.this);
 			file = null;
 		}
 
 		@Override
 		File process(Hole... params) throws LocalizedException {
 			Hole hole = params[0];
 			File file;
 			HashMap<String, String> form = new HashMap<String, String>();
 			form.put("login", getLogin());
 			form.put("password", getPassword());
 			form.put("to", hole.getTo());
 			form.put("from", getFrom());
 			form.put("postaddress", getPostAddress());
 			form.put("holeaddress", hole.getAddress());
 			form.put("signature", getSignature());
 			HttpEntity entity = client
 					.post(String.format(XML_HOST + "/my/%s/pdf_gibdd/",
 							hole.getId()), form, null);
 			file = new File(Environment.getExternalStorageDirectory(),
 					hole.getId() + ".pdf");
 			FileOutputStream stream;
 			try {
 				stream = new FileOutputStream(file);
 				entity.writeTo(stream);
 			} catch (IOException e) {
 				throw new LocalizedException(R.string.pdf_fail);
 			}
 			client.consumeEntity(entity);
 			return file;
 		}
 
 		@Override
 		void onClear() {
 			file = null;
 		}
 
 		@Override
 		void onSuccess(File result) {
 			file = result;
 		}
 
 		/**
 		 * Возвращает полученный файл.
 		 * 
 		 * @return
 		 */
 		public File getFile() {
 			return file;
 		}
 	}
 
 	/**
 	 * Определение адреса по координатам.
 	 * 
 	 * @author alexander.ivanov
 	 * 
 	 */
 	public class AddressOperation extends AbstractOperation<Location, String> {
 		private String address;
 
 		public AddressOperation() {
 			super(Rosyama.this);
 			address = null;
 		}
 
 		private String getAddressString(Element element) {
 			for (Element geoObjectCollection : new ElementIterable(
 					element.getElementsByTagName("GeoObjectCollection")))
 				for (Element featureMember : new ElementIterable(
 						geoObjectCollection
 								.getElementsByTagName("featureMember")))
 					for (Element geoObject : new ElementIterable(
 							featureMember.getElementsByTagName("GeoObject")))
 						for (Element metaDataProperty : new ElementIterable(
 								geoObject
 										.getElementsByTagName("metaDataProperty")))
 							for (Element geocoderMetaData : new ElementIterable(
 									metaDataProperty
 											.getElementsByTagName("GeocoderMetaData")))
 								for (Element text : new ElementIterable(
 										geocoderMetaData
 												.getElementsByTagName("text")))
 									return Client.getTextContent(text);
 			return null;
 		}
 
 		@Override
 		String process(Location... params) throws LocalizedException {
 			Location location = params[0];
 			if (location == null)
 				return "";
 			Element element = client.getCheckedElement(client.get(String
 					.format(YANDEX_ADDRESS, YANDEX_KEY,
 							getPoint(location.getLongitude()),
 							getPoint(location.getLatitude()))));
 			String address = getAddressString(element);
 			if (address == null)
 				throw new LocalizedException(R.string.data_fail);
 			return address;
 		}
 
 		@Override
 		void onClear() {
 			address = null;
 		}
 
 		@Override
 		void onSuccess(String result) {
 			address = result;
 		}
 
 		/**
 		 * Возвращает полученный адрес.
 		 * 
 		 * @return
 		 */
 		public String getAddress() {
 			return address;
 		}
 	}
 
 	/**
 	 * Определение координат по адресу.
 	 * 
 	 * @author alexander.ivanov
 	 * 
 	 */
 	public class LocationOperation extends AbstractOperation<String, Location> {
 		private Location location;
 
 		public LocationOperation() {
 			super(Rosyama.this);
 			location = null;
 		}
 
 		private String getLocationString(Element element) {
 			for (Element geoObjectCollection : new ElementIterable(
 					element.getElementsByTagName("GeoObjectCollection")))
 				for (Element featureMember : new ElementIterable(
 						geoObjectCollection
 								.getElementsByTagName("featureMember")))
 					for (Element geoObject : new ElementIterable(
 							featureMember.getElementsByTagName("GeoObject")))
 						for (Element point : new ElementIterable(
 								geoObject.getElementsByTagName("Point")))
 							for (Element pos : new ElementIterable(
 									point.getElementsByTagName("pos")))
 								return Client.getTextContent(pos);
 			return null;
 		}
 
 		@Override
 		Location process(String... params) throws LocalizedException {
 			String address = params[0];
 			if ("".equals(address))
				return null;
 			Element element;
 			try {
 				element = client.getCheckedElement(client.get(String.format(
 						YANDEX_LOCATION, YANDEX_KEY,
 						URLEncoder.encode(address, "UTF-8"))));
 			} catch (UnsupportedEncodingException e) {
 				throw new LocalizedException(R.string.data_fail);
 			}
 			String position = getLocationString(element);
 			if (position == null)
 				throw new LocalizedException(R.string.data_fail);
 			String[] values = position.split(" ");
 			if (values.length != 2)
 				throw new LocalizedException(R.string.data_fail);
 			Location location = new Location(LocationManager.GPS_PROVIDER);
 			try {
 				location.setLongitude(Double.valueOf(values[0]));
 				location.setLatitude(Double.valueOf(values[1]));
 			} catch (NumberFormatException e) {
 				throw new LocalizedException(R.string.data_fail);
 			}
 			return location;
 		}
 
 		@Override
 		void onClear() {
 			location = null;
 		}
 
 		@Override
 		void onSuccess(Location result) {
 			location = result;
 		}
 
 		/**
 		 * Возвращает полученную точку.
 		 * 
 		 * @return
 		 */
 		public Location getLocation() {
 			return location;
 		}
 	}
 
 	private static class LoginAndPassword {
 		final String login;
 		final String password;
 
 		LoginAndPassword(String login, String password) {
 			super();
 			this.login = login;
 			this.password = password;
 		}
 	}
 
 	private static class HoleAndHead {
 		final Hole hole;
 		final String head;
 
 		public HoleAndHead(Hole hole, String head) {
 			super();
 			this.hole = hole;
 			this.head = head;
 		}
 	}
 
 	private static class HoleAndHole {
 		final Hole source;
 		final Hole result;
 
 		public HoleAndHole(Hole source, Hole result) {
 			super();
 			this.source = source;
 			this.result = result;
 		}
 	}
 
 	/**
 	 * Производит разбор дефекта.
 	 * 
 	 * @param hole
 	 *            Елемент DOM-а, содержащий дефект.
 	 * @return
 	 * @throws LocalizedException
 	 */
 	private Hole parseHole(Element hole) throws LocalizedException {
 		String id = hole.getAttribute("id");
 		String latitude = null;
 		String longitude = null;
 		String address = null;
 		String state = null;
 		String type = null;
 		String datecreated = null;
 		String commentfresh = null;
 		ArrayList<String> original = new ArrayList<String>();
 		ArrayList<String> small = new ArrayList<String>();
 		for (Element property : new ElementIterable(hole.getChildNodes())) {
 			if ("latitude".equals(property.getTagName()))
 				latitude = Client.getTextContent(property);
 			if ("longitude".equals(property.getTagName()))
 				longitude = Client.getTextContent(property);
 			if ("address".equals(property.getTagName()))
 				address = Client.getTextContent(property);
 			if ("state".equals(property.getTagName()))
 				state = property.getAttribute("code");
 			if ("type".equals(property.getTagName()))
 				type = property.getAttribute("code");
 			if ("datecreated".equals(property.getTagName()))
 				datecreated = Client.getTextContent(property);
 			if ("commentfresh".equals(property.getTagName()))
 				commentfresh = Client.getTextContent(property);
 			if ("pictures".equals(property.getTagName()))
 				for (Element group : new ElementIterable(
 						property.getChildNodes())) {
 					ArrayList<String> current = null;
 					if ("original".equals(group.getTagName()))
 						current = original;
 					if ("small".equals(group.getTagName()))
 						current = small;
 					if (current == null)
 						continue;
 					for (Element fresh : new ElementIterable(
 							group.getElementsByTagName("fresh")))
 						for (Element src : new ElementIterable(
 								fresh.getElementsByTagName("src")))
 							current.add(Client.getTextContent(src));
 				}
 		}
 
 		if (latitude == null || longitude == null)
 			throw new LocalizedException(R.string.data_fail);
 		Location location = new Location(LocationManager.GPS_PROVIDER);
 		try {
 			location.setLatitude(Double.valueOf(latitude));
 			location.setLongitude(Double.valueOf(longitude));
 		} catch (NumberFormatException e) {
 			throw new LocalizedException(R.string.data_fail);
 		}
 
 		Type selectedType = null;
 		for (Type check : Type.values())
 			if (check.toString().equals(type))
 				selectedType = check;
 		if (selectedType == null)
 			throw new LocalizedException(R.string.data_fail);
 
 		ru.redsolution.rosyama.data.Status selectedStatus = null;
 		for (ru.redsolution.rosyama.data.Status check : ru.redsolution.rosyama.data.Status
 				.values())
 			// Чудесное API кое-где использует термин state, а
 			// кое-где status :)
 			if (check.toString().equals(state))
 				selectedStatus = check;
 		if (selectedStatus == null)
 			throw new LocalizedException(R.string.data_fail);
 
 		if (original.size() != small.size())
 			throw new LocalizedException(R.string.data_fail);
 		ArrayList<RemotePhoto> photos = new ArrayList<RemotePhoto>();
 		for (int index = 0; index < original.size(); index++)
 			photos.add(new RemotePhoto(Rosyama.this, Uri.parse(WEB_HOST
 					+ original.get(index)), Uri.parse(WEB_HOST
 					+ small.get(index))));
 
 		if (datecreated == null)
 			throw new LocalizedException(R.string.data_fail);
 		Date created;
 		try {
 			created = new Date(Long.valueOf(datecreated) * 1000);
 		} catch (NumberFormatException e) {
 			throw new LocalizedException(R.string.data_fail);
 		}
 
 		if (address == null || commentfresh == null)
 			throw new LocalizedException(R.string.data_fail);
 
 		return new Hole(Rosyama.this, id, created, location, address,
 				selectedType, selectedStatus, commentfresh, photos);
 	}
 
 	@Override
 	public void onUpdate() {
 		if (updateListener != null)
 			updateListener.onUpdate();
 	}
 
 	@Override
 	public void onLocationChanged(Location arg0) {
 	}
 
 	@Override
 	public void onProviderDisabled(String arg0) {
 	}
 
 	@Override
 	public void onProviderEnabled(String arg0) {
 	}
 
 	@Override
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 	}
 
 	private static String getPoint(double value) {
 		return Location.convert(value, Location.FORMAT_DEGREES).replace(',',
 				'.');
 	}
 
 	public static String getNextFileName(String extention) {
 		return Rosyama.FILE_NAME_FORMAT.format(new Date()) + extention;
 	}
 
 	public static Uri getNextJpegUri() {
 		return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
 				getNextFileName(".jpeg")));
 	}
 }
