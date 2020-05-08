 package ru.redsolution.bst.data;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 
 import net.iharder.base64.Base64;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthenticationException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.AbstractHttpClient;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import ru.redsolution.bst.R;
 import ru.redsolution.bst.data.parse.DocumentImporter;
 import ru.redsolution.bst.data.serializer.BaseSerializer;
 import ru.redsolution.bst.data.serializer.DemandSerializer;
 import ru.redsolution.bst.data.serializer.InventorySerializer;
 import ru.redsolution.bst.data.serializer.SupplySerializer;
 import ru.redsolution.bst.data.table.CompanyFolderTable;
 import ru.redsolution.bst.data.table.CompanyTable;
 import ru.redsolution.bst.data.table.ContractTable;
 import ru.redsolution.bst.data.table.DatabaseHelper;
 import ru.redsolution.bst.data.table.GoodBarcodeTable;
 import ru.redsolution.bst.data.table.GoodFolderTable;
 import ru.redsolution.bst.data.table.GoodTable;
 import ru.redsolution.bst.data.table.MyCompanyTable;
 import ru.redsolution.bst.data.table.ProjectTable;
 import ru.redsolution.bst.data.table.SelectedProductCodeForBarcodeTable;
 import ru.redsolution.bst.data.table.SelectedTable;
 import ru.redsolution.bst.data.table.UomTable;
 import ru.redsolution.bst.data.table.WarehouseTable;
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 
 /**
  * Класс приложения.
  * 
  * @author alexander.ivanov
  * 
  */
 public class BST extends Application {
 
 	private static final String HOST_URL = "https://online.moysklad.ru";
 	private static final String IMPORT_URL = HOST_URL
 			+ "/exchange/xml/export?name=Dictionary";
 	private static final String INVENTORY_URL = HOST_URL
 			+ "/exchange/rest/ms/xml/Inventory";
 
 	private static BST instance;
 
 	public static BST getInstance() {
 		if (instance == null)
 			throw new IllegalStateException();
 		return instance;
 	}
 
 	private SharedPreferences settings;
 	private ImportTask importTask;
 	private SendTask sendTask;
 	private OperationListener operationListener;
 
 	private final AbstractHttpClient httpClient;
 
 	public BST() {
 		super();
 		if (instance != null)
 			throw new IllegalStateException();
 		instance = this;
 		httpClient = new TrustedHttpClient(this);
 		importTask = null;
 		sendTask = null;
 		operationListener = null;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		settings = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 
 		// Создает singleton-ы
 		DatabaseHelper.getInstance();
 		CompanyFolderTable.getInstance();
 		CompanyTable.getInstance();
 		GoodTable.getInstance();
 		GoodFolderTable.getInstance();
 		GoodBarcodeTable.getInstance();
 		MyCompanyTable.getInstance();
 		UomTable.getInstance();
 		WarehouseTable.getInstance();
 		ProjectTable.getInstance();
 		ContractTable.getInstance();
 		SelectedTable.getInstance();
 		SelectedProductCodeForBarcodeTable.getInstance();
 	}
 
 	public void setOperationListener(OperationListener operationListener) {
 		this.operationListener = operationListener;
 	}
 
 	/**
 	 * Отправляет запрос, добавляя информацию для авторизации.
 	 * 
 	 * @param request
 	 * @return
 	 */
 	private HttpResponse executeRequest(HttpUriRequest request) {
 		request.setHeader(
 				"Authorization",
 				"Basic "
 						+ Base64.encodeBytes((getLogin() + ":" + getPassword())
 								.getBytes()));
 		HttpResponse response;
 		try {
 			response = httpClient.execute(request);
 		} catch (ClientProtocolException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
		if (response.getStatusLine().getStatusCode() != 200)
 			throw new RuntimeException(new AuthenticationException(response
 					.getStatusLine().toString()));
 		return response;
 	}
 
 	/**
 	 * @return Происходит ли сейчас импорт данных.
 	 */
 	public boolean isImporting() {
 		return importTask != null;
 	}
 
 	/**
 	 * Импорт данных.
 	 */
 	public void importData() {
 		if (isImporting())
 			return;
 		importTask = new ImportTask();
 		importTask.execute();
 	}
 
 	/**
 	 * Отмена импорта данных.
 	 */
 	public void cancelImport() {
 		if (!isImporting())
 			return;
 		importTask.cancel(true);
 	}
 
 	/**
 	 * @return Происходит ли сейчас отправка данных.
 	 */
 	public boolean isSending() {
 		return sendTask != null;
 	}
 
 	/**
 	 * Отправка данных.
 	 */
 	public void sendData() {
 		if (isSending())
 			return;
 		sendTask = new SendTask();
 		sendTask.execute();
 	}
 
 	/**
 	 * Отмена импорта данных.
 	 */
 	public void cancelSend() {
 		if (!isSending())
 			return;
 		sendTask.cancel(true);
 	}
 
 	/**
 	 * @return Производился ли импорт.
 	 */
 	public boolean isImported() {
 		return settings.getBoolean(getString(R.string.imported_key), false);
 	}
 
 	/**
 	 * @param resourceId
 	 * @return Значение поля настройки.
 	 */
 	private String getValue(int resourceId) {
 		return settings.getString(getString(resourceId), "");
 	}
 
 	/**
 	 * Устанавливает значение поля настройки.
 	 * 
 	 * @param resourceId
 	 * @param value
 	 */
 	private void setValue(int resourceId, String value) {
 		Editor editor = settings.edit();
 		editor.putString(getString(resourceId), value);
 		editor.commit();
 	}
 
 	public String getLogin() {
 		return getValue(R.string.login_key);
 	}
 
 	public String getPassword() {
 		return getValue(R.string.password_key);
 	}
 
 	public void setLoginAndPassword(String login, String password) {
 		Editor editor = settings.edit();
 		editor.putString(getString(R.string.login_key), login);
 		editor.putString(getString(R.string.password_key), password);
 		editor.commit();
 		httpClient.getCookieStore().clear();
 	}
 
 	public String getDefaultMyCompany() {
 		return getValue(R.string.default_my_company_key);
 	}
 
 	public void setDefaultMyCompany(String value) {
 		setValue(R.string.default_my_company_key, value);
 	}
 
 	public String getDefaultWarehouse() {
 		return getValue(R.string.default_warehouse_key);
 	}
 
 	public void setDefaultWarehouse(String value) {
 		setValue(R.string.default_warehouse_key, value);
 	}
 
 	public String getDefaultSupplyCompany() {
 		return getValue(R.string.default_supply_company_key);
 	}
 
 	public void setDefaultSupplyCompany(String value) {
 		setValue(R.string.default_supply_company_key, value);
 	}
 
 	public String getDefaultSupplyContract() {
 		return getValue(R.string.default_supply_contract_key);
 	}
 
 	public void setDefaultSupplyContract(String value) {
 		setValue(R.string.default_supply_contract_key, value);
 	}
 
 	public String getDefaultDemandCompany() {
 		return getValue(R.string.default_demand_company_key);
 	}
 
 	public void setDefaultDemandCompany(String value) {
 		setValue(R.string.default_demand_company_key, value);
 	}
 
 	public String getDefaultDemandContract() {
 		return getValue(R.string.default_demand_contract_key);
 	}
 
 	public void setDefaultDemandContract(String value) {
 		setValue(R.string.default_demand_contract_key, value);
 	}
 
 	public String getDefaultProject() {
 		return getValue(R.string.default_project_key);
 	}
 
 	public void setDefaultProject(String value) {
 		setValue(R.string.default_project_key, value);
 	}
 
 	/**
 	 * @return Тип редактируемого документа. <code>null</code>, если тип не
 	 *         выбран.
 	 */
 	public DocumentType getDocumentType() {
 		try {
 			return DocumentType.valueOf(getValue(R.string.document_type));
 		} catch (IllegalArgumentException e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Устанавливает тип документа.
 	 * 
 	 * <b>Приводит к удалению списка выбранных товаров.</b>
 	 * 
 	 * @param documentType
 	 *            Может быть <code>null</code>.
 	 */
 	public void setDocumentType(DocumentType documentType) {
 		if (documentType == null)
 			setValue(R.string.document_type, "");
 		else
 			setValue(R.string.document_type, documentType.name());
 		SelectedTable.getInstance().clear();
 		SelectedProductCodeForBarcodeTable.getInstance().clear();
 	}
 
 	public String getSelectedMyCompany() {
 		return getValue(R.string.selected_my_company_key);
 	}
 
 	public void setSelectedMyCompany(String value) {
 		setValue(R.string.selected_my_company_key, value);
 	}
 
 	public String getSelectedWarehouse() {
 		return getValue(R.string.selected_warehouse_key);
 	}
 
 	public void setSelectedWarehouse(String value) {
 		setValue(R.string.selected_warehouse_key, value);
 	}
 
 	public String getSelectedCompany() {
 		return getValue(R.string.selected_company_key);
 	}
 
 	public void setSelectedCompany(String value) {
 		setValue(R.string.selected_company_key, value);
 	}
 
 	public String getSelectedContract() {
 		return getValue(R.string.selected_contract_key);
 	}
 
 	public void setSelectedContract(String value) {
 		setValue(R.string.selected_contract_key, value);
 	}
 
 	public String getSelectedProject() {
 		return getValue(R.string.selected_project_key);
 	}
 
 	public void setSelectedProject(String value) {
 		setValue(R.string.selected_project_key, value);
 	}
 
 	/**
 	 * Абстрактная асинхронная задача, которая может завершиться успешно, либо
 	 * вернуть ошибку.
 	 * 
 	 * @author alexander.ivanov
 	 * 
 	 */
 	private abstract class AbstractTask extends
 			AsyncTask<Void, Void, RuntimeException> {
 
 		protected abstract void executeInBackground();
 
 		@Override
 		protected final RuntimeException doInBackground(Void... params) {
 			try {
 				executeInBackground();
 			} catch (RuntimeException e) {
 				return e;
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			if (operationListener != null)
 				operationListener.onBegin();
 		}
 
 		@Override
 		protected void onCancelled() {
 			super.onCancelled();
 			if (operationListener != null)
 				operationListener.onCancelled();
 		}
 
 		@Override
 		protected void onPostExecute(RuntimeException result) {
 			super.onPostExecute(result);
 			if (operationListener != null)
 				if (result == null)
 					operationListener.onDone();
 				else {
 					if (Debugger.ENABLED)
 						result.printStackTrace();
 					operationListener.onError(result);
 				}
 		}
 
 	}
 
 	/**
 	 * Задача импорта данных.
 	 * 
 	 * @author alexander.ivanov
 	 * 
 	 */
 	private class ImportTask extends AbstractTask {
 
 		@Override
 		protected void executeInBackground() {
 			CompanyFolderTable.getInstance().clear();
 			CompanyTable.getInstance().clear();
 			GoodTable.getInstance().clear();
 			GoodFolderTable.getInstance().clear();
 			GoodBarcodeTable.getInstance().clear();
 			MyCompanyTable.getInstance().clear();
 			UomTable.getInstance().clear();
 			WarehouseTable.getInstance().clear();
 			ProjectTable.getInstance().clear();
 			ContractTable.getInstance().clear();
 			Editor editor = settings.edit();
 			editor.putBoolean(getString(R.string.imported_key), false);
 			editor.commit();
 			HttpResponse response = executeRequest(new HttpGet(IMPORT_URL));
 			XmlPullParser parser;
 			try {
 				parser = XmlPullParserFactory.newInstance().newPullParser();
 				Reader reader = new BufferedReader(new InputStreamReader(
 						response.getEntity().getContent(), "UTF-8"));
 				if (Debugger.ENABLED) {
 					File dir = new File(
 							Environment.getExternalStorageDirectory(), "bst");
 					dir.mkdirs();
 					File file = new File(dir, new Date().getTime() + ".xml");
 					reader = new DebugReader(reader, file);
 				}
 				parser.setInput(reader);
 			} catch (IllegalStateException e) {
 				throw new RuntimeException(e);
 			} catch (XmlPullParserException e) {
 				throw new RuntimeException(e);
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 			try {
 				new DocumentImporter().parse(parser);
 			} catch (XmlPullParserException e) {
 				throw new RuntimeException(e);
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 			editor = settings.edit();
 			editor.putBoolean(getString(R.string.imported_key), true);
 			editor.commit();
 		}
 
 		@Override
 		protected void onCancelled() {
 			importTask = null;
 			super.onCancelled();
 		}
 
 		@Override
 		protected void onPostExecute(RuntimeException result) {
 			importTask = null;
 			super.onPostExecute(result);
 		}
 
 	}
 
 	/**
 	 * Задача отправки документа.
 	 * 
 	 * @author alexander.ivanov
 	 * 
 	 */
 	private class SendTask extends AbstractTask {
 
 		@Override
 		protected void executeInBackground() {
 			HttpPut request = new HttpPut(INVENTORY_URL);
 			BaseSerializer serializer;
 			if (getDocumentType() == DocumentType.supply)
 				serializer = new SupplySerializer();
 			else if (getDocumentType() == DocumentType.inventory)
 				serializer = new InventorySerializer();
 			else if (getDocumentType() == DocumentType.demand)
 				serializer = new DemandSerializer();
 			else
 				throw new UnsupportedOperationException();
 			String body;
 			try {
 				body = serializer.getXml();
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalStateException e) {
 				throw new RuntimeException(e);
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 			StringEntity entity;
 			try {
 				entity = new StringEntity(body, "UTF-8");
 			} catch (UnsupportedEncodingException e) {
 				throw new RuntimeException(e);
 			}
 			entity.setContentType("application/xml");
 			request.setEntity(entity);
 			executeRequest(request);
 			BST.getInstance().setDocumentType(null);
 		}
 
 		@Override
 		protected void onCancelled() {
 			sendTask = null;
 			super.onCancelled();
 		}
 
 		@Override
 		protected void onPostExecute(RuntimeException result) {
 			sendTask = null;
 			super.onPostExecute(result);
 		}
 
 	}
 
 }
