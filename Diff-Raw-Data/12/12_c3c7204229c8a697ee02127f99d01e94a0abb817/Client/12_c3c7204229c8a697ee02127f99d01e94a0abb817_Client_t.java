 package ru.redsolution.rosyama.data;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import ru.redsolution.rosyama.R;
 import android.content.Context;
 
 /**
  * Класс, обслуживающий запросы к серверу.
  * 
  * @author alexander.ivanov
  * 
  */
 public class Client {

 	/**
 	 * Писать заголовки запросов в catlog.
 	 */
 	public static final boolean LOG = Rosyama.DEBUG;

 	/**
 	 * Писать запросы и ответы в файлы.
 	 */
 	private static final boolean WRITE = Rosyama.DEBUG;
 
 	/**
 	 * Неправильный логин и/или пароль.
 	 */
 	private static final Object WRONG_CREDENTIALS = "WRONG_CREDENTIALS";
 
 	/**
 	 * Требуется авторизация (не прошла авторизация).
 	 */
 	private static final Object AUTHORIZATION_REQUIRED = "AUTHORIZATION_REQUIRED";
 
 	/**
 	 * Не возможно добавить деффект.
 	 */
 	private static final Object CANNOT_ADD_DEFECT = "CANNOT_ADD_DEFECT";
 
 	/**
 	 * Приложение.
 	 */
 	private final Rosyama rosyama;
 
 	/**
 	 * Фабрика для DOM парсера.
 	 */
 	private final DocumentBuilderFactory documentBuilderFactory;
 
 	public Client(Rosyama rosyama) {
 		this.rosyama = rosyama;
 		documentBuilderFactory = DocumentBuilderFactory.newInstance();
 	}
 
 	/**
 	 * Возвращает прочитанный контент. Не забудьте вызвать
 	 * {@link #consumeEntity(HttpEntity)} после использования контента.
 	 * 
 	 * @param request
 	 *            Обычно {@link HttpPost} или {@link HttpGet}.
 	 * @return
 	 * @throws LocalizedException
 	 */
 	private HttpEntity getResponse(HttpUriRequest request)
 			throws LocalizedException {
 		try {
 			if (LOG)
 				System.out.println("Request: "
 						+ request.getRequestLine().toString());
 			DefaultHttpClient client = new DefaultHttpClient();
 			HttpResponse response = client.execute(request);
 			if (LOG)
 				System.out.println("Response: " + response.getStatusLine());
 			HttpEntity entity = response.getEntity();
 			if (entity == null)
 				throw new LocalizedException(R.string.data_fail);
 			return entity;
 		} catch (ClientProtocolException e) {
 			if (LOG)
 				e.printStackTrace();
 			throw new LocalizedException(R.string.connection_fail);
 		} catch (IOException e) {
 			if (LOG)
 				e.printStackTrace();
 			throw new LocalizedException(R.string.io_fail);
 		}
 	}
 
 	/**
 	 * Освобождает ресурсы, занятые контентом запроса или ответа.
 	 * 
 	 * @param entity
 	 * @throws LocalizedException
 	 */
 	public void consumeEntity(HttpEntity entity) throws LocalizedException {
 		try {
 			entity.consumeContent();
 		} catch (IOException e) {
 			if (LOG)
 				e.printStackTrace();
 			throw new LocalizedException(R.string.io_fail);
 		}
 	}
 
 	/**
 	 * Возвращает контент для поста с данными из формы. Не забудьте вызвать
 	 * {@link #consumeEntity(HttpEntity)} после отправки запроса.
 	 * 
 	 * @param form
 	 *            Содержит имена аргументов и их значения.
 	 * @return
 	 */
 	private HttpEntity getPostEntity(Map<String, List<String>> form) {
 		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 		for (Entry<String, List<String>> entry : form.entrySet())
 			for (String value : entry.getValue())
 				nameValuePairs
 						.add(new BasicNameValuePair(entry.getKey(), value));
 		UrlEncodedFormEntity encodedFormEntity;
 		try {
 			encodedFormEntity = new UrlEncodedFormEntity(nameValuePairs,
 					HTTP.UTF_8);
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 		return encodedFormEntity;
 	}
 
 	/**
 	 * Возвращает контент для с данными из формы и контентом из файлов. Не
 	 * забудьте вызвать {@link #consumeEntity(HttpEntity)} после отправки
 	 * запроса.
 	 * 
 	 * @param form
 	 *            Содержит имена аргументов и их значения.
 	 * @param files
 	 *            Содержит имена файлов и путь до файла.
 	 * @return
 	 */
 	private HttpEntity getPostEntity(Map<String, List<String>> form,
 			Map<String, String> files) {
 		MultipartEntity multipartEntity = new MultipartEntity(
 				HttpMultipartMode.BROWSER_COMPATIBLE);
 		// Не следует передавать в конструктор boundary и charset.
 		try {
 			for (Entry<String, List<String>> entry : form.entrySet())
 				for (String value : entry.getValue())
 					multipartEntity.addPart(entry.getKey(), new StringBody(
 							value, Charset.forName(HTTP.UTF_8)));
 			for (Entry<String, String> entry : files.entrySet())
 				multipartEntity.addPart(entry.getKey(), new FileBody(new File(
 						entry.getValue()), "image/jpeg"));
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 		return multipartEntity;
 	}
 
 	/**
 	 * Добавляет контент в запрос и логирует действие.
 	 * 
 	 * @param post
 	 * @param entity
 	 */
 	private void setEntity(HttpPost post, HttpEntity entity) {
 		if (WRITE) {
 			try {
 				FileOutputStream stream;
 				stream = rosyama.openFileOutput(
 						Rosyama.getNextFileName(".post"),
 						Context.MODE_WORLD_WRITEABLE);
 				entity.writeTo(stream);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		post.setEntity(entity);
 	}
 
 	/**
 	 * @param form
 	 *            Содержит имена аргументов и одно его значения.
 	 * @return форму с именами и множеством значений.
 	 */
 	public static Map<String, List<String>> createListForm(
 			Map<String, String> form) {
 		if (form == null)
 			return null;
 		Map<String, List<String>> map = new HashMap<String, List<String>>();
 		for (Map.Entry<String, String> entry : form.entrySet()) {
 			List<String> list = new ArrayList<String>();
 			list.add(entry.getValue());
 			map.put(entry.getKey(), list);
 		}
 		return map;
 	}
 
 	/**
 	 * Отправляет POST запрос и возвращает прочитанный контент. Не забудьте
 	 * вызвать {@link #consumeEntity(HttpEntity)} после использования контента.
 	 * 
 	 * @param uri
 	 *            Адрес ресурса.
 	 * @param form
 	 *            Содержит имена аргументов и их значения. Если
 	 *            <code>null</code>, в контент не будет одобавлен в пост.
 	 * @param files
 	 *            Содержит имена файлов и путь до файла. Если не
 	 *            <code>null</code>, будет отправлен multipart запрос.
 	 * @return
 	 * @throws LocalizedException
 	 */
 	public HttpEntity post(String uri, Map<String, List<String>> form,
 			Map<String, String> files) throws LocalizedException {
 		HttpPost request = new HttpPost(uri);
 		HttpEntity postEntity;
 		if (form == null)
 			postEntity = null;
 		else {
 			if (files == null)
 				postEntity = getPostEntity(form);
 			else
 				postEntity = getPostEntity(form, files);
 			setEntity(request, postEntity);
 		}
 		try {
 			return getResponse(request);
 		} finally {
 			if (postEntity != null)
 				consumeEntity(postEntity);
 		}
 	}
 
 	/**
 	 * Отправляет GET запрос и возвращает прочитанный контент. Не забудьте
 	 * вызвать {@link #consumeEntity(HttpEntity)} после использования контента.
 	 * 
 	 * @param uri
 	 *            Адрес ресурса.
 	 * @return
 	 * @throws LocalizedException
 	 */
 	public HttpEntity get(String uri) throws LocalizedException {
 		return getResponse(new HttpGet(uri));
 	}
 
 	/**
 	 * Возвращает контент из {@link HttpEntity} в виде строки и освобождает
 	 * выделенные ресурсы.
 	 * 
 	 * @param entity
 	 * @return
 	 * @throws LocalizedException
 	 */
 	public String getString(HttpEntity entity) throws LocalizedException {
 		try {
 			String content = EntityUtils.toString(entity);
 			if (WRITE) {
 				try {
 					FileOutputStream stream = rosyama.openFileOutput(
 							Rosyama.getNextFileName(".raw"),
 							Context.MODE_WORLD_WRITEABLE);
 					stream.write(content.getBytes());
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			return content;
 		} catch (IOException e) {
 			if (LOG)
 				e.printStackTrace();
 			throw new LocalizedException(R.string.io_fail);
 		} finally {
 			consumeEntity(entity);
 		}
 	}
 
 	/**
 	 * Возвращает корневой DOM элемент.
 	 * 
 	 * @param post
 	 * @param entity
 	 * @return
 	 * @throws LocalizedException
 	 */
 	public Element getDOMElement(HttpEntity entity) throws LocalizedException {
 		String content = getString(entity);
 		Document dom;
 		try {
 			DocumentBuilder builder = documentBuilderFactory
 					.newDocumentBuilder();
 			dom = builder.parse(new ByteArrayInputStream(content
 					.getBytes("UTF-8")));
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		} catch (ParserConfigurationException e) {
 			if (LOG)
 				e.printStackTrace();
 			throw new LocalizedException(R.string.data_fail);
 		} catch (SAXException e) {
 			if (LOG)
 				e.printStackTrace();
 			throw new LocalizedException(R.string.data_fail);
 		} catch (IOException e) {
 			if (LOG)
 				e.printStackTrace();
 			throw new LocalizedException(R.string.io_fail);
 		}
 		return dom.getDocumentElement();
 	}
 
 	/**
 	 * Возвращает корневой DOM элемент и проверяет на наличие сообщений об
 	 * ошибоках от сервиса rosyama.
 	 * 
 	 * @param entity
 	 * @return
 	 * @throws LocalizedException
 	 */
 	public Element getCheckedElement(HttpEntity entity)
 			throws LocalizedException {
 		Element element = getDOMElement(entity);
 		NodeList nodeList = element.getElementsByTagName("error");
 		String lastError = null;
 		for (Element error : new ElementIterable(nodeList)) {
 			String code = error.getAttribute("code");
 			if (LOG)
 				System.out.println(code);
 			if (WRONG_CREDENTIALS.equals(code))
 				throw new LocalizedException(R.string.auth_fail);
 			else if (AUTHORIZATION_REQUIRED.equals(code))
 				throw new LocalizedException(R.string.auth_required);
 			else if (CANNOT_ADD_DEFECT.equals(code))
 				throw new LocalizedException(R.string.cannot_add_defect,
 						getTextContent(error));
 			else
 				lastError = getTextContent(error);
 		}
 		if (lastError != null) {
 			if ("".equals(lastError))
 				throw new LocalizedException(R.string.data_fail);
 			else
 				throw new LocalizedException(R.string.server_error, lastError);
 		}
 		return element;
 	}
 
 	/**
 	 * Получает текстовое значение, хронящееся в ноде.
 	 * 
 	 * @param node
 	 * @return
 	 */
 	public static String getTextContent(Node node) {
 		StringBuffer buffer = new StringBuffer();
 		NodeList chars = node.getChildNodes();
 		for (int piece = 0; piece < chars.getLength(); piece++)
 			buffer.append(chars.item(piece).getNodeValue());
 		return buffer.toString();
 	}
 }
