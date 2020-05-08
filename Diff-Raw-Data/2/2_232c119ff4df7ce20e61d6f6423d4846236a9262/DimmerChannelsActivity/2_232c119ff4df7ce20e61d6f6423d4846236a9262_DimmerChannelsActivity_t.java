 package be.abollaert.smartlights.android.client;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import be.abollaert.domotics.light.api.ChannelState;
 import be.abollaert.domotics.light.api.DimmerChannelStateChangeListener;
 import be.abollaert.domotics.light.api.DimmerInputChannelConfiguration;
 import be.abollaert.domotics.light.api.DimmerModule;
 
 public final class DimmerChannelsActivity extends BaseActivity {
 	
 	private Map<DimmerModule, DimmerChannelStateChangeListener> listeners = new HashMap<DimmerModule, DimmerChannelStateChangeListener>();
 
 	@Override
 	protected final void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		this.setTitle("Smartlights : Dimmer channels");
 		
 		final LinearLayout layout = new LinearLayout(this);
 		layout.setOrientation(LinearLayout.VERTICAL);
 		layout.setPadding(15, 15, 15, 15);
 		
 		final Handler handler = new Handler() {
 			public final void handleMessage(final Message msg) {
 				final int channelNumber = msg.what;
 				final int percentage = msg.arg1;
 				final boolean state = (msg.arg2 != 0);
 				final DimmerModule module = (DimmerModule)msg.obj;
 				
 				final int boxId = module.getId() * 10 + channelNumber;
 				final CheckBox checkBox = (CheckBox)findViewById(boxId);
 				
 				if (checkBox != null) {
 					checkBox.setChecked(state);
 				}
 				
 				final int percentageViewId = module.getId() * 100 + channelNumber;
 				final TextView percentageView = (TextView)findViewById(percentageViewId);
 				
 				if (percentageView != null) {
					percentageView.setText(String.valueOf(percentage));
 				}
 			}
 		};
 		
 		try {
 			for (final DimmerModule module : this.getDriver().getAllDimmerModules()) {
 				final DimmerChannelStateChangeListener listener = new DimmerChannelStateChangeListener() {
 					
 					@Override
 					public final void outputChannelStateChanged(final int channelNumber, final ChannelState newState, final int percentage) {
 						final Message message = new Message();
 						message.what = channelNumber;
 						message.arg1 = percentage;
 						message.arg2 = (newState == ChannelState.ON ? 1 : 0);
 						message.obj = module;
 						
 						handler.sendMessage(message);
 					}
 					
 					@Override
 					public void inputChannelStateChanged(int channelNumber, ChannelState newState) {
 					}
 				};
 				
 				module.addChannelStateListener(listener);
 				this.listeners.put(module, listener);
 				
 				for (int channelNumber = 0; channelNumber < module.getDimmerConfiguration().getNumberOfChannels(); channelNumber++) {
 					final DimmerInputChannelConfiguration config = module.getDimmerConfiguration().getDimmerChannelConfiguration(channelNumber);
 					
 					if (config.getName() != null && !config.getName().trim().equals("")) {
 						layout.addView(this.createRow(module, channelNumber));
 					}
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		this.setContentView(layout);
 	}
 	
 	
 	@Override
 	protected final void onDestroy() {
 		super.onDestroy();
 		
 		for (final DimmerModule module : this.listeners.keySet()) {
 			module.removeChannelStateListener(this.listeners.get(module));
 		}
 	}
 
 	private final View createRow(final DimmerModule module, final int channelId) {
 		final RelativeLayout layout = new RelativeLayout(this);
 		layout.setPadding(5, 5, 5, 5);
 
 		final CheckBox onOffBox = new CheckBox(this);
 		
 		try {
 			onOffBox.setChecked(module.getOutputChannelState(channelId) == ChannelState.ON);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		onOffBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 			@Override
 			public final void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
 				try {
 					if (isChecked) {
 						module.switchOutputChannel(channelId, ChannelState.ON);
 					} else {
 						module.switchOutputChannel(channelId, ChannelState.OFF);
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		RelativeLayout.LayoutParams layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		layoutParameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 		
 		onOffBox.setLayoutParams(layoutParameters);
 		onOffBox.setId(module.getId() * 10 + channelId);
 		
 		final TextView nameView = new TextView(this);
 		nameView.setText(module.getDimmerConfiguration().getDimmerChannelConfiguration(channelId).getName());
 		nameView.setTypeface(null, Typeface.BOLD);
 		
 		layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		layoutParameters.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		layoutParameters.addRule(RelativeLayout.ALIGN_BASELINE, onOffBox.getId());
 		
 		nameView.setLayoutParams(layoutParameters);
 		
 		final TextView percentageView = new TextView(this);
 		percentageView.setId(module.getId() * 100 + channelId);
 		percentageView.setTypeface(null, Typeface.BOLD);
 		percentageView.setPadding(0, 0, 10, 0);
 		
 		try {
 			percentageView.setText(String.valueOf(module.getDimmerPercentage(channelId)));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		layoutParameters.addRule(RelativeLayout.ALIGN_BASELINE, onOffBox.getId());
 		layoutParameters.addRule(RelativeLayout.LEFT_OF, onOffBox.getId());
 		
 		percentageView.setLayoutParams(layoutParameters);
 		
 		final SeekBar newPercentagePicker = new SeekBar(this);
 		newPercentagePicker.setMax(100);
 		newPercentagePicker.setId(1000 * module.getId() + channelId);
 		newPercentagePicker.setPadding(50, 0, 50, 0);
 		
 		try {
 			newPercentagePicker.setProgress(module.getDimmerPercentage(channelId));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		newPercentagePicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 			@Override
 			public final void onStopTrackingTouch(final SeekBar seekBar) {
 				final int percentage = newPercentagePicker.getProgress();
 				
 				try {
 					System.out.println("Dimming.");
 					module.dim(channelId, (short)percentage);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			@Override
 			public final void onStartTrackingTouch(final SeekBar seekBar) {
 			}
 			
 			@Override
 			public final void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
 				percentageView.setText(String.valueOf(progress));
 			}
 		});
 		
 		layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		layoutParameters.addRule(RelativeLayout.BELOW, onOffBox.getId());
 		newPercentagePicker.setLayoutParams(layoutParameters);
 		
 		layout.addView(nameView);
 		layout.addView(onOffBox);
 		layout.addView(percentageView);
 		layout.addView(newPercentagePicker);
 		
 		return layout;
 	}
 
 	
 }
