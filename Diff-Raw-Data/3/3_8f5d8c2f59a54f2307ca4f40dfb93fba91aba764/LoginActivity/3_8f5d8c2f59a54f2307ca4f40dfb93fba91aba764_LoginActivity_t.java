   package wa.android.common.activity;
 
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import nc.vo.wa.component.login.GroupData;
 import nc.vo.wa.component.login.LoginVO;
 import nc.vo.wa.component.struct.Action;
 import nc.vo.wa.component.struct.Actions;
 import nc.vo.wa.component.struct.DeviceInfoVO;
 import nc.vo.wa.component.struct.ParamTagVO;
 import nc.vo.wa.component.struct.ReqParamsVO;
 import nc.vo.wa.component.struct.ResDataVO;
 import nc.vo.wa.component.struct.ResResultVO;
 import nc.vo.wa.component.struct.ServiceCodeRes;
 import nc.vo.wa.component.struct.SessionInfo;
 import nc.vo.wa.component.struct.SessionParamsVO;
 import nc.vo.wa.component.struct.WAComponentInstanceVO;
 import nc.vo.wa.component.struct.WAComponentInstancesVO;
 import wa.android.common.App;
 import wa.android.common.Module;
 import wa.android.constants.ActionTypes;
 import wa.android.constants.ComponentIds;
 import wa.android.constants.Servers;
 import wa.android.constants.WAIntents;
 import wa.android.constants.WAPreferences;
 import wa.android.u8.inquire.R;
 
 import wa.framework.component.network.VOHttpResponse;
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.util.DisplayMetrics;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 @SuppressLint({ "ShowToast", "ShowToast", "ShowToast", "ShowToast", "ShowToast" })
 public class LoginActivity extends BaseActivity {
 
 	private ProgressDialog loginProgressDialog;
 	private ProgressDialog accountProgressDialog;
 
 	private Button loginBtn, setConnectionBtn;
 	private CheckBox autoLoginCheckBox;
 
 	private EditText usrNameEditText, usrPassEditText;
 	private TextView loginErrorTextView, accountsetTextView;
 
 	public static String VO_CACHE_LOGIN = "VO_CACHE_LOGIN";
 
 	private ArrayList<GroupData> accountsetList;
 	private boolean hasAccountSet = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		initialViews();
 	}
 
 	private void initialViews() {
 		setContentView(R.layout.activity_login);
 		boolean isAutoLogin = readPreference(WAPreferences.IS_AUTOLOGIN).equalsIgnoreCase("true");
 
 		accountProgressDialog = new ProgressDialog(this);
 		accountProgressDialog.setTitle("加载账套");
 		accountProgressDialog.setMessage("加载中...");
 		accountProgressDialog.setCancelable(false);
 		accountProgressDialog.setIndeterminate(true);
 
 		// loginProgressDialog = ProgressDialog.show(LoginActivity.this, "登陆",
 		// "登录中...", true, false);
 		loginProgressDialog = new ProgressDialog(LoginActivity.this);
 		loginProgressDialog.setTitle("登录");
 		loginProgressDialog.setMessage("登录中...");
 		loginProgressDialog.setIndeterminate(true);
 		loginProgressDialog.setCancelable(false);
 
 		accountsetTextView = (TextView) findViewById(R.id.login_accountset_selectTextView);
 		accountsetTextView.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				onAccountSetSelectBtnClicked();
 			}
 		});
 		String accountSet = readPreference(WAPreferences.GROUP_NAME);
 		if (isAutoLogin && accountSet != null && !"".equals(accountSet.trim())) {
 			accountsetTextView.setText(accountSet);
 			accountsetTextView.setBackgroundResource(R.drawable.login_accountset_button_enable_bg);
 			hasAccountSet = true;
 		}
 
 		loginBtn = (Button) findViewById(R.id.login_loginBtn);
 		loginBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (autoLoginCheckBox.isChecked()) {
 					writePreference(WAPreferences.USER_NAME, usrNameEditText.getText().toString());
 					writePreference(WAPreferences.USER_PASS, usrPassEditText.getText().toString());
 				}
 				//if (hasAccountSet) {
				if(usrNameEditText.getText().toString().equals("") || usrPassEditText.getText().toString().equals(""))
					toastMsg("用户名或密码不能为空");
				else
 					login();
 			//	} else {
 			//		toastMsg("请选择一个账套");
 			//	}
 			}
 		});
 		setConnectionBtn = (Button) findViewById(R.id.login_setconnectionBtn);
 		setConnectionBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(LoginActivity.this, SetConnectionActivity.class);
 				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(i);
 			}
 		});
 
 		autoLoginCheckBox = (CheckBox) findViewById(R.id.login_autoLogin_checkBox);
 		autoLoginCheckBox.setChecked(isAutoLogin);
 		autoLoginCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				synchronized (autoLoginCheckBox) {
 					writePreference(WAPreferences.IS_AUTOLOGIN, isChecked ? "true" : "false");
 					if (isChecked) {
 						writePreference(WAPreferences.USER_NAME, usrNameEditText.getText().toString());
 						writePreference(WAPreferences.USER_PASS, usrPassEditText.getText().toString());
 					} else {
 						writePreference(WAPreferences.USER_NAME, "");
 						writePreference(WAPreferences.USER_PASS, "");
 					}
 				}
 			}
 		});
 
 		usrNameEditText = (EditText) findViewById(R.id.login_usernameEditText);
 		usrPassEditText = (EditText) findViewById(R.id.login_passwordEditText);
 		if (isAutoLogin) {
 			usrNameEditText.setText(readPreference(WAPreferences.USER_NAME));
 			usrPassEditText.setText(readPreference(WAPreferences.USER_PASS));
 			// login();
 		}
 
 		// usrNameEditText.setOnKeyListener(new View.OnKeyListener() {
 		// @Override
 		// public boolean onKey(View v, int keyCode, KeyEvent event) {
 		// if (!usrNameEditText.getText().toString().trim().equalsIgnoreCase("")
 		// && !usrPassEditText.getText().toString().trim().equalsIgnoreCase(""))
 		// {
 		// accountsetTextView.setBackgroundResource(R.drawable.login_accountset_button_enable_bg);
 		// }else{
 		// accountsetTextView.setBackgroundResource(R.drawable.login_accountset_button_disable_bg);
 		// }
 		// return false;
 		// }
 		// });
 		//
 		// usrPassEditText.setOnKeyListener(new View.OnKeyListener() {
 		// @Override
 		// public boolean onKey(View v, int keyCode, KeyEvent event) {
 		// if (!usrNameEditText.getText().toString().trim().equalsIgnoreCase("")
 		// && !usrPassEditText.getText().toString().trim().equalsIgnoreCase(""))
 		// {
 		// accountsetTextView.setBackgroundResource(R.drawable.login_accountset_button_enable_bg);
 		// }else{
 		// accountsetTextView.setBackgroundResource(R.drawable.login_accountset_button_disable_bg);
 		// }
 		// return false;
 		// }
 		// });
 
 		loginErrorTextView = (TextView) findViewById(R.id.login_errorTextView);
 		loginErrorTextView.setVisibility(View.INVISIBLE);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			Intent i = new Intent(this, WelcomeActivity.class);
 			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			i.putExtra("finish", true);
 			startActivity(i);
 			finish();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	private void onAccountSetSelectBtnClicked() {
 		if (isRequesting)
 			return;
 		fetchAccountsetList(new OnVORequestedListener() {
 			@Override
 			public void onVORequested(VOHttpResponse vo) {
 				System.out.println(vo);
 				ResResultVO resResultVO = null;
 				try {
 					resResultVO = vo.getmWAComponentInstancesVO().getWaci().get(0).getActions().getActions().get(0).getResresulttags();
 				} catch (NullPointerException e) {
 					e.printStackTrace();
 					toastMsg("返回结果错误");
 				}
 				if (resResultVO.getFlag() == 0) {
 					accountsetList = new ArrayList<GroupData>();
 					for (Object o : resResultVO.getServcieCodesRes().getScres().get(0).getResdata().getList()) {
 						accountsetList.add((GroupData) o);
 					}
 					ArrayList<String> setList = new ArrayList<String>();
 					for (GroupData g : accountsetList) {
 						setList.add(g.getGroupname());
 					}
 					String[] setArray = new String[setList.size()];
 					setList.toArray(setArray);
 					new AlertDialog.Builder(LoginActivity.this).setTitle("选择账套").setItems(setArray, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							accountsetTextView.setText(accountsetList.get(which).getGroupname());
 							writePreference(WAPreferences.GROUP_CODE, accountsetList.get(which).getGroupid());
 							writePreference(WAPreferences.GROUP_NAME, accountsetList.get(which).getGroupname());
 							hasAccountSet = true;
 						}
 					}).setNegativeButton("取消", null).show();
 				} else {
 					toastMsg("账套获取失败：" + resResultVO.getDesc());
 				}
 				accountProgressDialog.dismiss();
 			}
 
 			@Override
 			public void onVORequestFailed(VOHttpResponse vo) {
 				if (vo != null)
 					toastMsg("账套获取失败：" + vo.getStatusCode());
 				accountProgressDialog.dismiss();
 			}
 		});
 		if (accountsetList == null) {
 			return;
 		}
 	}
 
 	/**
 	 * 
 	 * 获取帐套列表
 	 */
 	private void fetchAccountsetList(OnVORequestedListener l) {
 		String name = usrNameEditText.getText().toString();
 		String pass = usrPassEditText.getText().toString();
 		if ("".equals(name) || "".equals(pass)) {
 			toastMsg("用户名密码不能为空");
 			return;
 		}
 		WAComponentInstancesVO mWAComponentInstancesVO = new WAComponentInstancesVO();
 		ArrayList<WAComponentInstanceVO> waComponentInstanceVOList = new ArrayList<WAComponentInstanceVO>();
 		WAComponentInstanceVO waComponentInstanceVO = new WAComponentInstanceVO();
 		waComponentInstanceVO.setComponentid(ComponentIds.WA00000);
 		Actions actions = new Actions();
 		ArrayList<Action> actionList = new ArrayList<Action>();
 		Action action = new Action();
 		action.setActiontype(ActionTypes.GET_ACCOUNT);
 		ReqParamsVO paramVO = new ReqParamsVO();
 		ArrayList<ParamTagVO> params = new ArrayList<ParamTagVO>();
 		params.add(new ParamTagVO("password", name));
 		params.add(new ParamTagVO("usrcode", pass));
 		paramVO.setParamlist(params);
 		action.setParamstags(paramVO);
 		actionList.add(action);
 		actions.setActions(actionList);
 		waComponentInstanceVO.setActions(actions);
 		waComponentInstanceVOList.add(waComponentInstanceVO);
 		mWAComponentInstancesVO.setWaci(waComponentInstanceVOList);
 
 		accountProgressDialog.show();
 		requestVO(Servers.getServerAddress(LoginActivity.this) + Servers.SERVER_SERVLET_GETACCOUNTSET, mWAComponentInstancesVO, l);
 
 	}
 
 	@SuppressLint("HandlerLeak")
 	private void login() {
 		App.Log('d', LoginActivity.class, "start login");
 		loginProgressDialog.show();
 		requestVO(Servers.getServerAddress(LoginActivity.this) + Servers.SERVER_SERVLET_LOGIN, createLoginVOWtihRequestOfMainActivityAppend(),
 				new OnVORequestedListener() {
 					@Override
 					public void onVORequested(VOHttpResponse vo) {
 						App.Log('d', LoginActivity.class, "login responsed");
 						loginProgressDialog.dismiss();
 
 						// TODO: 这里有大量可能会为空的变量一定要处理。
 
 						WAComponentInstancesVO wacisVO = vo.getmWAComponentInstancesVO();
 
 						if (wacisVO == null) {
 							App.Log('e', LoginActivity.class, "componentinstancesVO in resResultVO is null ! ");
 							toastMsg("erro login response");
 						} else {
 							// sessionId is used to request business VO
 							String sessionId = wacisVO.getSp().getSessionid();
 							writePreference(WAPreferences.SESSION_ID_SP, sessionId);
 							int flag = -1;
 							for (WAComponentInstanceVO waciVO : wacisVO.getWaci()) {
 								// judge if the component id is yours
 								if (waciVO != null && ComponentIds.WA00001.equalsIgnoreCase(waciVO.getComponentid())) {
 									for (Action action : waciVO.getActions().getActions()) {
 										// judge if the action type is yours
 										if (action != null && ActionTypes.LOGIN.equalsIgnoreCase(action.getActiontype())) {
 											// you get the your(login) response
 											// action here
 											// all your data is in the
 											// ResResultVO
 											// packaged in response action
 											ResResultVO resResultVO = action.getResresulttags();
 											// don't know if this will happen,
 											// handle as more null pointer
 											// exception
 											// as you can
 											if (resResultVO == null) {
 												App.Log('e', LoginActivity.class, "resResultVO is null ! ");
 												toastMsg("erro login response");
 											} else {
 												// flag:
 												// 0 succeed
 												// -1 error when WA server
 												// invoke
 												// other servers
 												// >0 other businesses errors
 												flag = resResultVO.getFlag();
 												String desc = resResultVO.getDesc();
 												switch (flag) {
 												case 0:
 													for (ServiceCodeRes serviceCodeRes : resResultVO.getServcieCodesRes().getScres()) {
 														String servicecode = serviceCodeRes.getServicecode();
 														String srcname = serviceCodeRes.getSrcname();
 														String productid = serviceCodeRes.getProductid();
 														writePreference(WAPreferences.SERVICE_CODE, servicecode);
 														writePreference(WAPreferences.PRODUCT_ID, productid);
 
 														ResDataVO resDataVO = serviceCodeRes.getResdata();
 														for (Object resData : resDataVO.getList()) {
 															if (resData != null && resData instanceof LoginVO) {
 																String attsize = ((LoginVO) resData).getAttsize();
 																String userId = ((LoginVO) resData).getUsrid();
 																String userName = ((LoginVO) resData).getUsrname();
 																String groupCode = ((LoginVO) resData).getGroupcode();
 																String groupId = ((LoginVO) resData).getGroupid();
 																String groupName = ((LoginVO) resData).getGroupname();
 																writePreference(WAPreferences.USER_ID, userId);
 																// writePreference(WAPreferences.USER_NAME,
 																// userName);
 																// session.setGroupCode(groupCode);
 																writePreference(WAPreferences.GROUP_ID, groupId);
 																// session.setGroupName(groupName);
 															}
 														}
 													}
 													break;
 												case -1:
 													loginErrorTextView.setVisibility(View.VISIBLE);
 													loginErrorTextView.setText(desc);
 													break;
 												case 1:
 													loginErrorTextView.setVisibility(View.VISIBLE);
 													loginErrorTextView.setText(desc);
 													break;
 												default:
 													break;
 												}
 												if (!"".equalsIgnoreCase(desc.trim())) {
 													toastMsg(desc);
 												} else {
 													App.Log('w', LoginActivity.class, "unknown error happend when login");
 													toastMsg("unknown error happend when login");
 												}
 											}
 										}
 									}
 								}
 							}
 
 							if (flag == 0) {
 								for (final Module m : App.moduleList) {
 									if (m != null) {
 										m.onLoginSuccessfully(vo.getmWAComponentInstancesVO());
 									}
 								}
 								Intent intent = WAIntents.getMAIN_ACTIVITY(getBaseContext());
 								startActivity(intent);
 								// finish();
 							}
 						}
 					}
 
 					@Override
 					public void onVORequestFailed(VOHttpResponse vo) {
 						App.Log('d', LoginActivity.class, "login fail responsed");
 						loginProgressDialog.dismiss();
 					}
 				});
 	}
 
 	/**
 	 * 将各个module需要在登陆时请求的信息一同封装在loginVO中
 	 * 若期望module携带自己的业务VO，请重写module的getAppendRequestVO()方法
 	 * 
 	 * @see Module
 	 * @return 用于请求的最终VO
 	 */
 	private WAComponentInstancesVO createLoginVOWtihRequestOfMainActivityAppend() {
 		String usrName = usrNameEditText.getText().toString();
 		String usrPass = usrPassEditText.getText().toString();
 
 		WAComponentInstancesVO loginVO = createLoginRequestVO(usrName, usrPass);
 		for (Module m : App.moduleList) {
 			WAComponentInstanceVO appendVO = m.getAppendRequestVO(readPreference(WAPreferences.GROUP_ID), readPreference(WAPreferences.USER_ID));
 			if (appendVO != null) {
 				loginVO.getWaci().add(appendVO);
 			}
 		}
 		return loginVO;
 	}
 
 	/**
 	 * @return a login request VO
 	 */
 	private WAComponentInstancesVO createLoginRequestVO(String usrName, String usrPass) {
 		// ////////////////////
 		// construct the VO instance and set content
 		WAComponentInstancesVO mWAComponentInstancesVO = new WAComponentInstancesVO();
 		mWAComponentInstancesVO.setDi(getDeviceInfoVO());
 
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 		// ///////////////////////
 		// 用于二次登陆（二次登陆时将本地保存的服务器信息上传，以减少服务器的查询响应时间）
 		String groupid = readPreference(WAPreferences.GROUP_ID);
 		if (!"".equalsIgnoreCase(groupid)) {
 			SessionInfo sp = new SessionInfo();
 			List<SessionParamsVO> loginfoList = new ArrayList<SessionParamsVO>();
 			SessionParamsVO sessionParamsVOnew = new SessionParamsVO();
 			sp.setSessionid(readPreference(WAPreferences.SESSION_ID_SP));
 			String productid = readPreference(WAPreferences.PRODUCT_ID);
 			String serviceid = readPreference(WAPreferences.SERVICE_CODE);
 			String usrid = readPreference(WAPreferences.USER_ID);
 			sessionParamsVOnew.setGroupid(groupid);
 			sessionParamsVOnew.setProductid(productid);
 			sessionParamsVOnew.setServiceid(serviceid);
 			sessionParamsVOnew.setUsrid(usrid);
 			sp.setLoginfo(loginfoList);
 			mWAComponentInstancesVO.setStime(new StringBuffer(df.format(new Date())));
 			mWAComponentInstancesVO.setSp(sp);
 		}
 		//
 		// ///////////////////////
 
 		ArrayList<WAComponentInstanceVO> waComponentInstanceVOList = new ArrayList<WAComponentInstanceVO>();
 		WAComponentInstanceVO waComponentInstanceVO = new WAComponentInstanceVO();
 		waComponentInstanceVO.setComponentid(ComponentIds.WA00001);
 		Actions actions = new Actions();
 		ArrayList<Action> actionList = new ArrayList<Action>();
 		Action action = new Action();
 		action.setActiontype(ActionTypes.LOGIN);
 		ReqParamsVO paramVO = new ReqParamsVO();
 		ArrayList<ParamTagVO> params = new ArrayList<ParamTagVO>();
 
 		params.add(new ParamTagVO("password", usrName));
 		params.add(new ParamTagVO("usrcode", usrPass));
 
 		// TODO: hard code the date to suit the real U8 server
 //	 	 params.add(new ParamTagVO("groupid", "(default)@001"));
 		params.add(new ParamTagVO("groupid", readPreference(WAPreferences.GROUP_CODE)));
 		params.add(new ParamTagVO("date", df.format(new Date())));
 		//params.add(new ParamTagVO("date", "2012-9-29"));
 
 		paramVO.setParamlist(params);
 		action.setParamstags(paramVO);
 		actionList.add(action);
 		actions.setActions(actionList);
 		waComponentInstanceVO.setActions(actions);
 		waComponentInstanceVOList.add(waComponentInstanceVO);
 		mWAComponentInstancesVO.setWaci(waComponentInstanceVOList);
 
 		// end construct
 		// /////////////////
 
 		return mWAComponentInstancesVO;
 	}
 
 	/**
 	 * 
 	 * 获得设备信息 生成对应的VO
 	 * 
 	 * @return
 	 */
 	private DeviceInfoVO getDeviceInfoVO() {
 		String dmString;// 分辨率
 		String imei;// imei
 		String osVersion;// 系统版本
 		String devLanguage;// 语言
 		String screenSi;// 实际尺寸
 		String wfAddress;// wifi的MAC地址
 		// 获取手机分辨率
 		DisplayMetrics dm = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(dm);
 		int width = dm.widthPixels;
 		int height = dm.heightPixels;
 		dmString = height + "x" + width;
 		// 获取实际尺寸
 		double x = Math.pow(width / dm.xdpi, 2);
 		double y = Math.pow(height / dm.ydpi, 2);
 		double screenInches = Math.sqrt(x + y);
 		NumberFormat nf = NumberFormat.getNumberInstance();
 		nf.setMaximumFractionDigits(1);
 		screenSi = String.valueOf(nf.format(screenInches));
 
 		// 获取imei
 		// 权限<uses-permission
 		// android:name="android.permission.READ_PHONE_STATE"/>
 		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 		imei = telephonyManager.getDeviceId();
 
 		// 获取操作系统版本
 		osVersion = android.os.Build.VERSION.RELEASE;
 
 		// 获取当前的语言
 		devLanguage = Locale.getDefault().getLanguage();
 
 		// 获取wifi地址
 		// <uses-permission
 		// android:name="android.permission.ACCESS_WIFI_STATE"/>
 		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);// 获取WifiManager
 		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
 		wfAddress = (wifiInfo == null ? "" : wifiInfo.getMacAddress());
 
 		// TODO: 该device info有问题 服务器无法处理
 		DeviceInfoVO deviceInfoVO = new DeviceInfoVO();
 		// deviceInfoVO.setAppid(App.APPID);
 		// deviceInfoVO.setAppVersion(App.APPVERSION);
 		// deviceInfoVO.setType("phone");//
 		// deviceInfoVO.setOs("android");
 		// deviceInfoVO.setResolution(dmString);
 		// deviceInfoVO.setOsversion(osVersion);
 		// deviceInfoVO.setDevlanguage(devLanguage);
 		// deviceInfoVO.setScreensi(screenSi);
 		// deviceInfoVO.setImie(imei);
 		// deviceInfoVO.setWFAddress(wfAddress);
 		//
 		// System.out.println(deviceInfoVO.getAppid());
 		// System.out.println(deviceInfoVO.getAppVersion());
 		// System.out.println(deviceInfoVO.getType());//
 		// System.out.println(deviceInfoVO.getOs());
 		// System.out.println(deviceInfoVO.getResolution());
 		// System.out.println(deviceInfoVO.getAppVersion());//
 		// System.out.println(deviceInfoVO.getOsversion());
 		// System.out.println(deviceInfoVO.getDevlanguage());
 		// System.out.println(deviceInfoVO.getScreensi());
 		// System.out.println(deviceInfoVO.getImie());
 		// System.out.println(deviceInfoVO.getAppid());//
 		// System.out.println(deviceInfoVO.getWFAddress());
 
 		// /////////////////////////////////////////////////////////
 		// 正确可运行的device info
 		deviceInfoVO.setAppid("SSTASK120521A");
 		deviceInfoVO.setAppVersion(1.0f);
 		// deviceInfoVO.setBTAddress("http://10.1.37.193:8090/servlet/");
 		// deviceInfoVO.setCarrier("jobject");
 		// deviceInfoVO.setDevicetoken("token");
 		deviceInfoVO.setDeviceType("android");
 		deviceInfoVO.setDevlanguage("zh-Hans");
 		// deviceInfoVO.setIccid("iccid");
 		deviceInfoVO.setImie("3CAE4544-A873-595B-A0CF-B9826BE9B406");
 		// deviceInfoVO.setImsi("imsi");
 		// deviceInfoVO.setIp("10.1.37.193");
 		deviceInfoVO.setOs("android");
 		deviceInfoVO.setOsversion("2.3");
 		// deviceInfoVO.setPhonenumber("13800010086");
 		deviceInfoVO.setResolution("800x480");
 		deviceInfoVO.setScreensi("4.3");
 		// deviceInfoVO.setSerial("serial");
 		deviceInfoVO.setType("phone");
 		// deviceInfoVO.setWFAddress("3C07545CD473");
 		// /////////////////////////////////////////////////////////
 		return deviceInfoVO;
 	}
 }
