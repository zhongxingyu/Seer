 package ru.tulupov.nsuconnect.fragment;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.text.Editable;
 import android.text.TextWatcher;
 
 import ru.tulupov.nsuconnect.database.DatabaseConstants;
 import ru.tulupov.nsuconnect.helper.BroadcastHelper;
 import ru.tulupov.nsuconnect.util.Log;
 
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.MapBuilder;
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
 
 import java.io.File;
 import java.sql.SQLException;
 
 import ru.tulupov.nsuconnect.R;
 import ru.tulupov.nsuconnect.database.ContentUriHelper;
 import ru.tulupov.nsuconnect.database.HelperFactory;
 import ru.tulupov.nsuconnect.helper.BitmapHelper;
 import ru.tulupov.nsuconnect.helper.IntentActionHelper;
 import ru.tulupov.nsuconnect.images.ImageCacheManager;
 import ru.tulupov.nsuconnect.model.Chat;
 import ru.tulupov.nsuconnect.service.DataService;
 
 
 public class ChatFragment extends BaseFragment {
     private static final int REQUEST_CODE_TAKE_PHOTO = 0;
     private static final int REQUEST_CODE_IMPORT_PHOTO = 1;
     private static final String ARGS_CHAT_ID = "chat_id";
     private static final String ARGS_IS_ACTIVE = "chat_is_active";
     private static final String TAG = ChatFragment.class.getSimpleName();
     private File photo;
     private boolean isChatActive;
 
     public static ChatFragment newInstance(final Context context, int chatId, boolean isChatActive) {
         final Bundle args = new Bundle();
         args.putInt(ARGS_CHAT_ID, chatId);
         args.putBoolean(ARGS_IS_ACTIVE, isChatActive);
         return (ChatFragment) Fragment.instantiate(context, ChatFragment.class.getName(), args);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fgt_chat, container, false);
     }
 
     private boolean isTyping;
     private int chatId;
 
     private BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if (chatId == intent.getIntExtra(BroadcastHelper.EXTRA_CHAT_ID, 0)) {
                 isChatActive = false;
                 getActivity().supportInvalidateOptionsMenu();
                 hideSendSection();
             }
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
 
         isChatActive = getArguments().getBoolean(ARGS_IS_ACTIVE);
     }
 
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 
         if (isChatActive) {
             inflater.inflate(R.menu.fgt_chat, menu);
        } else {
            inflater.inflate(R.menu.fgt_chat_2, menu);
         }
 
 
 //        SubMenu submenu = menu.addSubMenu(0, Menu.NONE, 1, "New Form").setIcon(R.drawable.ic_launcher);
 //        submenu.add("Form 1").setIcon(R.drawable.ic_launcher);
 
 
 //        SubMenu subMenu = menu.addSubMenu(0,   R.id.menu_attach_take_photo, Menu.NONE,R.string.menu_attach_take_photo);
 
 //        menu.addSubMenu(0,   R.id.menu_attach_take_photo, Menu.NONE,R.string.menu_attach_take_photo);
 //        menu.addSubMenu(0,  R.id.menu_attach_import_photo,Menu.NONE, R.string.menu_attach_import_photo);
 
         super.onCreateOptionsMenu(menu, inflater);
     }
 
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
 
             case android.R.id.home:
                 closeFragment();
                 break;
 
             case R.id.menu_close:
                 EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("UX", "chat", "close_chat", null).build());
                 closeFragment();
                 break;
 
             case R.id.menu_exit:
                 EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("UX", "chat", "exit_chat", null).build());
                 DialogConfirmExitFragment dialog = DialogConfirmExitFragment.newInstance(getActivity());
                 dialog.setOnExitListener(new DialogConfirmExitFragment.OnExitListener() {
                     @Override
                     public void onExit() {
                         EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("UX", "chat", "exit_chat_yes", null).build());
 
                         try {
                             getActivity().startService(new Intent(getActivity(), DataService.class)
                                     .setAction(DataService.ACTION_DESTROY_SESSION).putExtra(DataService.EXTRA_ID, chat.getId()));
 
                             HelperFactory.getHelper().getChatDao().deactivateChat(chat.getId());
                             ContentUriHelper.notifyChange(getActivity(), ContentUriHelper.getChatUri());
 
 
                         } catch (SQLException e) {
                             Log.e(TAG, "error deactivate chat", e);
                         }
                         closeFragment();
                     }
                 });
                 showDialog(dialog);
 
                 break;
 
 
             case R.id.menu_upload:
                 EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("UX", "chat", "attach_file", null).build());
 
                 DialogItemListFragment dialogItemList = DialogItemListFragment.newInstance(getActivity(), R.array.messages_attach_actions);
                 dialogItemList.setOnItemClickListener(new DialogItemListFragment.OnItemClickListener() {
                     @Override
                     public void onClick(int position) {
                         switch (position) {
                             case 0:
                                 EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("UX", "chat", "attach_take_photo", null).build());
                                 photo = BitmapHelper.getTempFile();
                                 startActivityForResult(IntentActionHelper.getCameraIntent(getActivity(), photo), REQUEST_CODE_TAKE_PHOTO);
                                 return;
                             case 1:
                                 EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("UX", "chat", "attach_import_photo", null).build());
 
                                 startActivityForResult(IntentActionHelper.getSelectFromGallery1Intent(getActivity()), REQUEST_CODE_IMPORT_PHOTO);
                                 return;
                         }
                     }
                 });
                 if (chat != null && chat.isActive()) {
                     showDialog(dialogItemList);
                 }
 
 
                 break;
 
 
             default:
                 return super.onOptionsItemSelected(item);
         }
         return true;
     }
 
     private Chat chat;
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         chatId = getArguments().getInt(ARGS_CHAT_ID);
         if (savedInstanceState == null) {
 
             getChildFragmentManager().beginTransaction()
                     .replace(R.id.message_container, ConversationFragment.newInstance(getActivity(), chatId))
                     .commit();
         }
 
 
 //        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
 //            @Override
 //            public void onClick(View view) {
 //                Message message = new Message();
 //                message.setMessage("fff " + System.currentTimeMillis());
 //                try {
 //                    HelperFactory.getHelper().getMessageDao().create(message);
 //
 //                    getActivity().sendBroadcast(new Intent(DatabaseConstants.ACTION_UPDATE_MESSAGE_LIST));
 //                } catch (SQLException e) {
 //                    Log.e("xxx", e.getLocalizedMessage());
 //                }
 //            }
 //        });
 
         final EditText edit = (EditText) view.findViewById(R.id.edit);
         view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (edit.getText().length() == 0) {
                     return;
                 }
                 getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_SEND_MESSAGE).putExtra(DataService.EXTRA_ID, chatId).putExtra(DataService.EXTRA_MESSAGE, edit.getText().toString()));
                 edit.setText(null);
 
                 EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("UX", "chat", "send_message", null).build());
 
             }
         });
 
         edit.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
 
             }
 
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 if (edit.getText().length() == 0) {
                     isTyping = false;
                     handler.removeCallbacks(stopTypingRunnable);
                     getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_STOP_TYPING).putExtra(DataService.EXTRA_ID, chatId));
                 } else {
                     if (!isTyping) {
                         getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_START_TYPING).putExtra(DataService.EXTRA_ID, chatId));
                         handler.removeCallbacks(stopTypingRunnable);
                         handler.postDelayed(stopTypingRunnable, TYPING_TIMEOUT);
                         isTyping = true;
                     }
                 }
 
             }
 
             @Override
             public void afterTextChanged(Editable editable) {
 
             }
         });
 
 
         final View rootView = getView();
 
         rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
             @Override
             public void onGlobalLayout() {
                 int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
                 SlidingMenu slidingMenu = getSlidingMenu();
                 if (slidingMenu != null) {
                     // detect if keyboard opened
                     if (heightDiff > 250) {
                         slidingMenu.setOnOpenListener(new SlidingMenu.OnOpenListener() {
                             @Override
                             public void onOpen() {
                                 if (getActivity() != null) {
                                     InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                             Context.INPUT_METHOD_SERVICE);
                                     imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                                 }
                             }
                         });
 
                     }
                 }
 
             }
         });
 
 
         try {
             chat = HelperFactory.getHelper().getChatDao().getChat(chatId);
             if (!chat.isActive()) {
                 hideSendSection();
             }
             getActivity().setTitle(chat.getName());
         } catch (SQLException e) {
             Log.e(TAG, "cannot load chat entity", e);
         }
 
 
     }
 
     private void hideSendSection() {
         findViewById(R.id.send_container).setVisibility(View.GONE);
         findViewById(R.id.shadow).setVisibility(View.GONE);
     }
 
     private Runnable stopTypingRunnable = new Runnable() {
         @Override
         public void run() {
             isTyping = false;
             if (getActivity() != null) {
                 getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_STOP_TYPING));
             }
         }
     };
 
     private static final long TYPING_TIMEOUT = 1500;
     private Handler handler = new Handler();
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
 
         if ((requestCode == REQUEST_CODE_TAKE_PHOTO || requestCode == REQUEST_CODE_IMPORT_PHOTO) && resultCode == Activity.RESULT_OK) {
             try {
                 String picturePath = BitmapHelper.getPicturePath(getActivity(), data);
                 if (picturePath == null) {
                     picturePath = photo.getPath();
                 }
 
                 Bitmap bitmap = BitmapHelper.getNormalPhoto(picturePath);
 
                 File file = BitmapHelper.saveBitmapToTmpFile(bitmap);
 
 
                 if (file != null) {
                     ImageCacheManager.getInstance().putBitmap(file.getPath(), bitmap);
                     getActivity().startService(new Intent(getActivity(), DataService.class).setAction(DataService.ACTION_SEND_MESSAGE)
                             .putExtra(DataService.EXTRA_FILE, file.getPath())
                             .putExtra(DataService.EXTRA_ID, chatId));
                 }
             } catch (Exception e) {
                 Log.e(TAG, "get the photo error", e);
             }
         }
         super.onActivityResult(requestCode, resultCode, data);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         IntentFilter updateTypingStatusFilter = new IntentFilter(BroadcastHelper.ACTION_USER_DISCONNECTED);
         getActivity().registerReceiver(connectivityReceiver, updateTypingStatusFilter);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         getActivity().unregisterReceiver(connectivityReceiver);
     }
 }
