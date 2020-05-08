 package com.foreveross.chameleon.pad.fragment;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.foreveross.chameleon.Application;
 import com.foreveross.chameleon.CubeAndroid;
 import com.csair.impc.R;
 import com.foreveross.chameleon.TmpConstants;
 import com.foreveross.chameleon.activity.FacadeActivity;
 import com.foreveross.chameleon.event.EventBus;
 import com.foreveross.chameleon.event.NoticesDeletedInMessageEvent;
 import com.foreveross.chameleon.event.PatchMessageModelEvent;
 import com.foreveross.chameleon.event.PresenceEvent;
 import com.foreveross.chameleon.phone.modules.ContentChangeListener;
 import com.foreveross.chameleon.phone.modules.CubeModule;
 import com.foreveross.chameleon.phone.modules.CubeModuleManager;
 import com.foreveross.chameleon.phone.modules.MessageAdapter;
 import com.foreveross.chameleon.phone.modules.MessageFragmentModel;
 import com.foreveross.chameleon.phone.modules.MsgModel;
 import com.foreveross.chameleon.push.client.Constants;
 import com.foreveross.chameleon.push.cubeparser.type.AbstractMessage;
 import com.foreveross.chameleon.util.FileCopeTool;
 import com.foreveross.chameleon.util.PadUtils;
 import com.foreveross.chameleon.util.UnkownUtil;
 import com.squareup.otto.Subscribe;
 import com.squareup.otto.ThreadEnforcer;
 
 /**
  * [消息模块显示界面 ]<BR>
  * [功能详细描述]
  * 
  * @author 冯伟立
  * @version [CubeAndroid, 2013-10-12]
  */
 public class MessageFragment extends Fragment {
 
 	private final static Logger log = LoggerFactory
 			.getLogger(MessageFragment.class);
 
 	/**
 	 * [导航左侧按钮（返回）]
 	 */
 	private Button titlebar_left;
 	/**
 	 * [导航右侧按钮（取消，编辑）]
 	 */
 	private Button titlebar_right;
 	/**
 	 * [标题内容]
 	 */
 	private TextView titlebar_content;
 	/**
 	 * [可伸展列表]
 	 */
 	private ExpandableListView msglist;
 	/**
 	 * [全选框]
 	 */
 	private CheckBox allselected;
 	/**
 	 * [编辑面板（最下面）]
 	 */
 	private RelativeLayout editcheckbox;
 
 	/**
 	 * [删除按钮]
 	 */
 	private Button delete;
 	/**
 	 * [已读按钮]
 	 */
 	private Button mark;
 
 	/**
 	 * [通知管理器 ]
 	 */
 	private NotificationManager notificationManager;
 	/**
 	 * [msglist对应数据源adpater]
 	 */
 	private MessageAdapter messageAdapter;
 
 	/**
 	 * [全局应用 ]
 	 */
 	private Application application;
 
 	/**
 	 * [本面板对应数据模型（单例）]
 	 */
 	private MessageFragmentModel messageFragmentModel = null;
 
 	private ContentChangeListener contentChangeListener = null;
 
 	/**
 	 * [初始化]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param savedInstanceState
 	 *            2013-8-20 下午4:40:30
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// * 得到application
 		application = Application.class.cast(this.getAssocActivity()
 				.getApplication());
 		// * 得到notificationManager
 		notificationManager = (NotificationManager) getAssocActivity()
 				.getSystemService(Context.NOTIFICATION_SERVICE);
 		// * 初始化页面模型
 		messageFragmentModel = MessageFragmentModel.instance();
 		// 注册监听器
 		contentChangeListener = new ContentChangeListener() {
 
 			@Override
 			public void onContentChange(final int unreadCount, int msgCount,
 					List<MsgModel> msgModelList) {
 				// 因为此方法可能处于非UI线程，保证运行于UI线程
 				UnkownUtil.runOnUIThread(
 						MessageFragment.this.getAssocActivity(),
 						new Runnable() {
 							@Override
 							public void run() {
 								ajustViewDisplay();
 								// changeMessageRecordCount(unreadCount);
 								// expendFirstMsgModel();
 								synchronized (messageFragmentModel) {
									messageAdapter.notifyDataSetChanged();	
 								}
 							}
 						});
 			}
 		};
 	}
 
 	/**
 	 * [一句话功能简述]<BR>
 	 * [功能详细描述] 2013-10-18 下午3:43:47
 	 */
 	@Override
 	public void onStart() {
 		// TODO Auto-generated method stub
 		super.onStart();
 		messageFragmentModel.addContentChangeListener(contentChangeListener);
 	}
 
 	/**
 	 * [控制编辑状态显示]<BR>
 	 * [功能详细描述] 2013-10-11 下午4:27:37
 	 */
 	private void ajustViewDisplay() {
 		List<MsgModel> msgModels = messageFragmentModel.getMessageData();
 		if (msgModels == null || msgModels.isEmpty()) {
 			// * 重置状态
 			uneditState(false);
 		} else {
 			if (messageFragmentModel.isEditing()) {
 				editState();
 			} else {
 				uneditState(true);
 			}
 		}
 	}
 
 	private void uneditState(boolean display) {
 		titlebar_right.setText("编辑");
 		if (display) {
 			titlebar_right.setVisibility(View.VISIBLE);
 		} else {
 			titlebar_right.setVisibility(View.GONE);
 		}
 		allselected.setChecked(messageFragmentModel.isSelected());
 		editcheckbox.setVisibility(View.GONE);
 	}
 
 	private void editState() {
 		titlebar_right.setText("取消");
 		titlebar_right.setVisibility(View.VISIBLE);
 		allselected.setChecked(messageFragmentModel.isSelected());
 		editcheckbox.setVisibility(View.VISIBLE);
 	}
 
 	/**
 	 * [展开第一组]<BR>
 	 * [功能详细描述] 2013-10-11 下午4:47:10
 	 */
 	private void expendFirstMsgModel() {
 		if (messageFragmentModel.getFirstMsgModelExpandState() != null
 				&& messageFragmentModel.getFirstMsgModelExpandState()) {
 			msglist.expandGroup(0);
 		}
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 		// * 注册到EVENTBUS_MESSAGE_CONTENT EventBus
 		EventBus.getEventBus(TmpConstants.EVENTBUS_MESSAGE_CONTENT,
 				ThreadEnforcer.MAIN).register(this);
 
 		return inflater.inflate(R.layout.message, null);
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 		titlebar_left = (Button) view.findViewById(R.id.title_barleft);
 		titlebar_left.setOnClickListener(clickListener);
 		titlebar_right = (Button) view.findViewById(R.id.title_barright);
 		titlebar_right.setOnClickListener(clickListener);
 		titlebar_right.setBackgroundResource(R.drawable.normal_button_clickbg);
 		titlebar_right.setText("编辑");
 		titlebar_content = (TextView) view.findViewById(R.id.title_barcontent);
 		titlebar_content.setText("消息推送");
 		msglist = (ExpandableListView) view.findViewById(R.id.msglist);
 		editcheckbox = (RelativeLayout) view.findViewById(R.id.editcheckbox);
 		allselected = (CheckBox) view.findViewById(R.id.allselected);
 		allselected.setOnCheckedChangeListener(checkedChangeListener);
 		delete = (Button) view.findViewById(R.id.delete);
 		delete.setOnClickListener(clickListener);
 		mark = (Button) view.findViewById(R.id.mark);
 		mark.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				messageFragmentModel.markSelectedMsgRead();
 			}
 		});
 		messageAdapter = new MessageAdapter(getAssocActivity(),
 				messageFragmentModel.getMessageData());
 		msglist.setGroupIndicator(null);
 		msglist.setAdapter(messageAdapter);
 		msglist.setOnChildClickListener(childClickListener);
 		notificationManager.cancel(Constants.MESSAGE_NOTIFY_ID);
 	}
 
 	/**
 	 * [onresume周期]<BR>
 	 * [功能详细描述] 2013-8-20 下午6:54:47
 	 */
 	@Override
 	public void onResume() {
 		super.onResume();
 		// *告知MessageFragment处于显示状态
 		EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).post(
 				new PresenceEvent(TmpConstants.VIEW_MESSAGE_PRESENCE, true));
 		// * 如果当前牌resume阶段，不应该发送message通知
 		application.setShouldSendMessageNotification(false);
 	}
 
 	/**
 	 * [onPause]<BR>
 	 * [功能详细描述] 2013-9-3 上午11:26:27
 	 */
 	@Override
 	public void onPause() {
 		super.onPause();
 		// * 如果当前牌pause阶段，应该发送message通知
 		application.setShouldSendMessageNotification(true);
 		messageFragmentModel.removeContentChangeListner(contentChangeListener);
 	}
 
 	/**
 	 * [一句话功能简述]<BR>
 	 * [功能详细描述] 2013-8-20 下午6:56:11
 	 */
 	@Override
 	public void onStop() {
 		super.onStop();
 		// *告知MessageFragment处于非显示状态
 		EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).post(
 				new PresenceEvent(TmpConstants.VIEW_MESSAGE_PRESENCE, false));
 
 	}
 
 	@Override
 	public void onDestroy() {
 
 		super.onDestroy();
 	}
 
 	/**
 	 * [收到删除消息的通知]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param noticesDeletedInMessageEvent
 	 *            2013-10-12 上午9:18:59
 	 */
 	@Subscribe
 	public void onNoticesDeletedInMessageEvent(
 			final NoticesDeletedInMessageEvent noticesDeletedInMessageEvent) {
 		messageFragmentModel.removeNotices(noticesDeletedInMessageEvent
 				.getPatch());
 	}
 
 	@Subscribe
 	public void onPatchMessageModelEvent(
 			final PatchMessageModelEvent patchMessageModelEvent) {
 		MessageFragmentModel.instance().addMessages(
 				patchMessageModelEvent.getPacked());
 
 	}
 
 	OnClickListener clickListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 
 			switch (v.getId()) {
 			case R.id.title_barleft: {
 				if (PadUtils.isPad(getAssocActivity())) {
 					((FacadeActivity) getAssocActivity()).popRight();
 				} else {
 					getAssocActivity().finish();
 				}
 				break;
 			}
 			case R.id.title_barright:
 				MessageFragmentModel.instance().setEditing(
 						!MessageFragmentModel.instance().isEditing());
 				break;
 			case R.id.delete:
 				if (!messageFragmentModel.hasSelected()) {
 					new AlertDialog.Builder(getAssocActivity()).setTitle("提示")
 							.setMessage("请选择删除项").setNegativeButton("确定", null)
 							.show();
 				} else {
 					new AlertDialog.Builder(getAssocActivity())
 							.setTitle("提示")
 							.setMessage("确定要删除么？")
 							.setPositiveButton("确定",
 									new DialogInterface.OnClickListener() {
 
 										@Override
 										public void onClick(
 												DialogInterface paramDialogInterface,
 												int paramInt) {
 											showCustomDialog(true);
 											messageFragmentModel
 													.deleteSelectedMsg();
 											cancelDialog();
 										}
 									}).setNegativeButton("取消", null).show();
 				}
 				break;
 			default:
 				break;
 			}
 		}
 	};
 
 	OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
 
 		@Override
 		public void onCheckedChanged(CompoundButton buttonView,
 				boolean isChecked) {
 			MessageFragmentModel.instance().setSelected(isChecked);
 
 		}
 	};
 
 	OnChildClickListener childClickListener = new OnChildClickListener() {
 
 		@Override
 		public boolean onChildClick(ExpandableListView parent, View v,
 				int groupPosition, int childPosition, long id) {
 			if (messageFragmentModel.isEditing()) {
 				messageFragmentModel.selectMessage(groupPosition,
 						childPosition, !messageFragmentModel.isSelected());
 			} else {
 				try {
 					AbstractMessage<?> am = messageFragmentModel
 							.getReadOnlyMessage(groupPosition, childPosition);
 					CubeModule module = CubeModuleManager.getInstance()
 							.getCubeModuleByIdentifier(am.getGroupBelong());
 					if (module == null) {
 						log.warn("获取模块失败");
 						return false;
 					}
 					if (module.getIdentifier().equals(
 							TmpConstants.ANNOUCE_RECORD_IDENTIFIER)) {
 						return false;
 					}
 
 					if (module != null) {
 						MessageFragmentModel.instance().readAllRecordsByModule(
 								module.getName());
 					}
 					if (module.getPushMsgLink() == 0) {
 						return false;
 					}
 					Intent intent = new Intent();
 					if (module.getModuleType() == CubeModule.INSTALLED
 							|| module.getModuleType() == CubeModule.UPGRADABLE) {
 
 						if (module.getLocal() != null) {
 							intent.setClassName(getAssocActivity(),
 									module.getLocal());
 							startActivity(intent);
 
 						} else {
 							String path = Environment
 									.getExternalStorageDirectory().getPath()
 									+ "/" + getAssocActivity().getPackageName();
 							String url = path + "/www/"
 									+ module.getIdentifier();
 							if (new FileCopeTool(getAssocActivity())
 									.isfileExist(url, "index.html")) {
 
 								intent.putExtra("from", "main");
 								intent.putExtra("identify",
 										module.getIdentifier());
 								intent.putExtra("path", path);
 								String recordId = am.getMesssageId();
 								if (recordId == null || "".equals(recordId)) {
 									Toast.makeText(getAssocActivity(),
 											"抱歉,旧数据不支持跳转", Toast.LENGTH_SHORT)
 											.show();
 									return false;
 								}
 								intent.putExtra("recordId", recordId);
 								intent.setClass(getAssocActivity(),
 										CubeAndroid.class);
 								startActivity(intent);
 							}
 						}
 					} else if (module.getModuleType() == CubeModule.UNINSTALL) {
 						Toast.makeText(getAssocActivity(), "文件缺失，请重新下载",
 								Toast.LENGTH_SHORT).show();
 					} else {
 						Toast.makeText(getAssocActivity(), "模块正在下载、安装、更新中，请稍后",
 								Toast.LENGTH_SHORT).show();
 					}
 				} catch (Exception e) {
 					Log.e("message", "跳转模块异常");
 				}
 			}
 
 			return false;
 		}
 	};
 
 	@Override
 	public void onDestroyView() {
 		super.onDestroyView();
 		EventBus.getEventBus(TmpConstants.EVENTBUS_MESSAGE_CONTENT,
 				ThreadEnforcer.MAIN).unregister(this);
 		messageFragmentModel.setEditing(false);
 	}
 
 	public Dialog progressDialog;
 
 	public void showCustomDialog(boolean cancelable) {
 		if (progressDialog == null) {
 			progressDialog = new Dialog(getAssocActivity(), R.style.dialog);
 			progressDialog.setContentView(R.layout.dialog_layout);
 		}
 
 		if (progressDialog.isShowing()) {
 			return;
 		}
 		progressDialog.setCancelable(cancelable);
 		progressDialog.show();
 	}
 
 	public void cancelDialog() {
 		if (progressDialog == null) {
 			return;
 		}
 		if (progressDialog.isShowing()) {
 			progressDialog.cancel();
 		}
 	}
 
 	@Override
 	public String toString() {
 		return this.getClass().getCanonicalName();
 	}
 }
