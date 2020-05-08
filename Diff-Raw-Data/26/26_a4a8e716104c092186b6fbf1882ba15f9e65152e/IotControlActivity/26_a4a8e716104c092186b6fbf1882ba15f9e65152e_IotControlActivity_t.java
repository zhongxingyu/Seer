 package com.oosic.iot.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.oosic.iot.controller.library.IotAdapter;
 import com.oosic.iot.controller.library.IotCommand;
 import com.oosic.iot.controller.library.IotCommandType;
 import com.oosic.iot.controller.library.IotDevice;
 import com.oosic.iot.controller.library.IotManager;
 import com.oosic.iot.controller.utils.UIUtils;
 import com.oosic.iot.controller.utils.Utils;
 
 import android.app.AlertDialog;
import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 public class IotControlActivity extends IotBaseActivity {
 
    private static final String TAG = "IotControlActivity";
 
    private Button mConfigBtn, mDeviceListBtn, mSearchBtn;
    private GridView mCommandGridView;
    private CommandAdapter mCommandAdapter;
    private DeviceAdapter mDeviceAdapter;
    private IotManager mIotManager;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
 
       setContentView(R.layout.activity_control);
 
       findViews();
       init();
    }
 
    private void findViews() {
       mConfigBtn = (Button) findViewById(R.id.config_btn);
       mDeviceListBtn = (Button) findViewById(R.id.device_list_btn);
       mSearchBtn = (Button) findViewById(R.id.search_btn);
       mCommandGridView = (GridView) findViewById(R.id.command_grid);
    }
 
    private void init() {
       mIotManager = getIotManager();
 
       initViews();
    }
 
    private void initViews() {
       mDeviceListBtn.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
             showDeviceListDialog();
          }
       });
 
       mSearchBtn.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
             // search devices
          }
       });
 
       mConfigBtn.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
             startActivity(new Intent(IotControlActivity.this,
                   IotConfigActivity.class));
          }
       });
 
       List<IotCommand> commands = new ArrayList<IotCommand>();
       for (int i = 0; i < 6; i++) {
          IotCommand cmd = new IotCommand("CD" + i);
          cmd.setType(IotCommandType.LOCAL_BROADCAST);
          commands.add(cmd);
       }
       mCommandAdapter = new CommandAdapter(this, commands);
       mCommandGridView.setAdapter(mCommandAdapter);
    }
 
    @Override
    public void onBackPressed() {
       super.onBackPressed();
    }
 
    private void showCommandConfigDialog(IotCommand cmd) {
       LayoutInflater inflater = LayoutInflater.from(this);
       ViewGroup view = (ViewGroup) inflater.inflate(R.layout.command_config,
             null);
       ViewGroup layout = (ViewGroup) view
             .findViewById(R.id.command_config_layout);
       if (layout != null) {
          layout.setTag(cmd);
       }
       EditText cmdView = (EditText) view.findViewById(R.id.command);
       if (cmdView != null) {
          cmdView.setTag(cmd);
          cmdView.setText(cmd.getCommand());
       }
       RadioGroup cmdTypeView = (RadioGroup) view
             .findViewById(R.id.command_type_selection);
       if (cmdTypeView != null) {
          cmdTypeView.setTag(cmd);
          if (IotCommandType.isLocalBroadcast(cmd.getType())) {
             cmdTypeView.check(R.id.local_broadcast);
          } else if (IotCommandType.isLocalPeerToPeer(cmd.getType())) {
             cmdTypeView.check(R.id.local_peer_to_peer);
          } else if (IotCommandType.isRemoteServer(cmd.getType())) {
             cmdTypeView.check(R.id.remote_server);
          }
 
          cmdTypeView
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                   public void onCheckedChanged(RadioGroup group, int checkedId) {
                      if (group.getTag() != null) {
                         IotCommand cmd = (IotCommand) group.getTag();
                         if (checkedId == R.id.local_broadcast) {
                            cmd.setType(IotCommandType.LOCAL_BROADCAST);
                         } else if (checkedId == R.id.local_peer_to_peer) {
                            cmd.setType(IotCommandType.LOCAL_PEER_TO_PEER);
                         } else if (checkedId == R.id.remote_server) {
                            cmd.setType(IotCommandType.REMOTE_SERVER);
                         }
                      }
                   }
                });
       }
 
       AlertDialog.Builder builder = UIUtils.getAlertDialogBuilder(this);
       builder
             .setIcon(R.drawable.icon)
             .setTitle(R.string.command_config)
             .setView(view)
             .setPositiveButton(R.string.ok,
                   new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int which) {
                        Dialog dlg = (Dialog) dialog;
                        EditText cmdView = (EditText) dlg
                              .findViewById(R.id.command);
                        if (cmdView != null) {
                           if (cmdView.getTag() != null) {
                              IotCommand cmd = (IotCommand) cmdView.getTag();
                              cmd.setCommand(cmdView.getText().toString());
                           }
                        }

                         mCommandAdapter.notifyDataSetChanged();
                      }
                   }).setNegativeButton(R.string.cancel, null).show();
    }
 
    private void showDeviceListDialog() {
       List<IotDevice> devices = mIotManager.getDeviceList();
       if (devices == null || devices.size() <= 0) {
          UIUtils.showToast(this, getString(R.string.no_device));
          return;
       }

       mDeviceAdapter = new DeviceAdapter(this, devices);
       LayoutInflater inflater = LayoutInflater.from(this);
       ListView listView = (ListView) inflater.inflate(R.layout.list, null);
       listView.setAdapter(mDeviceAdapter);
 
       AlertDialog.Builder builder = UIUtils.getAlertDialogBuilder(this);
       builder.setIcon(R.drawable.icon).setTitle(R.string.device_list)
             .setView(listView).show();
    }
 
    private class CommandAdapter extends IotAdapter<IotCommand> {
 
       public CommandAdapter(Context context) {
          super(context);
       }
 
       public CommandAdapter(Context context, List<IotCommand> data) {
          super(context, data);
       }
 
       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
          if (convertView == null) {
             LayoutInflater inflater = LayoutInflater.from(this.context);
             convertView = inflater.inflate(R.layout.command_cell, null);
          }
 
          if (this.data.size() <= 0 || position < 0
                || position >= this.data.size()) {
             return convertView;
          }
 
          IotCommand cmd = this.data.get(position);
          if (cmd != null) {
             Button cmdBtn = (Button) convertView.findViewById(R.id.command_btn);
             if (cmdBtn != null) {
                cmdBtn.setTag(cmd);
                cmdBtn.setText(cmd.getCommand());
                cmdBtn.setOnClickListener(new View.OnClickListener() {
                   public void onClick(View v) {
                      if (v.getTag() != null) {
                         IotCommand cmd = (IotCommand) v.getTag();
                         // send command
                      }
                   }
                });
                cmdBtn.setOnLongClickListener(new View.OnLongClickListener() {
                   public boolean onLongClick(View v) {
                      if (v.getTag() != null) {
                         IotCommand cmd = (IotCommand) v.getTag();
                         showCommandConfigDialog(cmd);
                      }
                      return false;
                   }
                });
             }
 
             TextView cmdDescView = (TextView) convertView
                   .findViewById(R.id.command_desc);
             if (cmdDescView != null) {
                cmdDescView
                      .setText(IotCommandType.getDescription(cmd.getType()));
             }
             convertView.setTag(cmd);
          }
 
          return convertView;
       }
 
    }
 
    private class DeviceAdapter extends IotAdapter<IotDevice> {
 
       public DeviceAdapter(Context context) {
          super(context);
       }
 
       public DeviceAdapter(Context context, List<IotDevice> data) {
          super(context, data);
       }
 
       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
          if (convertView == null) {
             LayoutInflater inflater = LayoutInflater.from(this.context);
             convertView = inflater.inflate(R.layout.device_cell, null);
          }
 
          if (this.data.size() <= 0 || position < 0
                || position >= this.data.size()) {
             return convertView;
          }
 
          IotDevice dev = this.data.get(position);
          if (dev != null) {
             TextView ipView = (TextView) convertView
                   .findViewById(R.id.device_ip);
             if (ipView != null) {
                ipView.setTag(dev);
                ipView.setText(dev.getIp());
             }
 
             CheckBox selView = (CheckBox) convertView
                   .findViewById(R.id.device_selection);
             if (selView != null) {
                selView.setTag(dev);
                selView.setChecked(dev.isSelected());
                selView
                      .setOnCheckedChangeListener(new OnCheckedChangeListener() {
                         public void onCheckedChanged(CompoundButton buttonView,
                               boolean isChecked) {
                            IotDevice dev = (IotDevice) buttonView.getTag();
                            if (dev != null) {
                               dev.setSelected(isChecked);
                            }
                         }
                      });
             }
 
             convertView.setTag(dev);
          }
 
          return convertView;
       }
 
    }
 
 }
