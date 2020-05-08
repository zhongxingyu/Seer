 /**
  * SettingsManager.java
  * Nov 27, 2011 11:19:28 AM
  */
 package mobi.cyann.deviltools;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import java.lang.Integer;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 /**
  * @author arif
  *
  */
 public class SettingsManager {
 	private final static String LOG_TAG = "DevilTools.SettingsManager";
 	
 	public final static int SUCCESS = 0;
 	public final static int ERR_SET_ON_BOOT_FALSE = -1;
 	public final static int ERR_DIFFERENT_KERNEL = -2;
 	
 	private static String buildCommand(Context c, SharedPreferences preferences) {
 		StringBuilder command = new StringBuilder();
 		
 		String status = null;
 		String filepath = null;
 		int value = -1;
 		if(!preferences.getBoolean(c.getString(R.string.key_default_voltage), true)) {
 			// restore voltage setting if and only if key_default_voltage is false
 			
 			// customvoltage
 			// -----------------
 			// arm voltages
 			value = preferences.getInt(c.getString(R.string.key_max_arm_volt), -1);
 			if(value > -1) {
 				String armvolts = preferences.getString(c.getString(R.string.key_arm_volt_pref), "0");
 				command.append("echo " + value + " > " + "/sys/class/misc/customvoltage/max_arm_volt\n");
 				command.append("echo " + armvolts + " > " + "/sys/class/misc/customvoltage/arm_volt\n");
 			}else {
 				// uv_mv_table
 				status = preferences.getString(c.getString(R.string.key_uvmvtable_pref), "-1");
 				if(!status.equals("-1")) {
 					command.append("echo " + status + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table\n");
 				}
 			}
 			// int voltages
 			value = preferences.getInt(c.getString(R.string.key_max_int_volt), -1);
 			if(value > -1) {
 				String armvolts = preferences.getString(c.getString(R.string.key_int_volt_pref), "0");
 				command.append("echo " + value + " > " + "/sys/class/misc/customvoltage/max_int_volt\n");
 				command.append("echo " + armvolts + " > " + "/sys/class/misc/customvoltage/int_volt\n");
 			}
 		}
 		// Audio
 		value = preferences.getInt(c.getString(R.string.key_speaker_tuning), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/speaker_tuning\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_speaker_offset), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/speaker_offset\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_headphone_amplifier_level), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/headphone_amplifier_level\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_stereo_expansion), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/stereo_expansion\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_stereo_expansion_gain), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/stereo_expansion_gain\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_headphone_eq), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/headphone_eq\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_headphone_eq_b1_gain), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/headphone_eq_b1_gain\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_headphone_eq_b2_gain), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/headphone_eq_b2_gain\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_headphone_eq_b3_gain), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/headphone_eq_b3_gain\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_headphone_eq_b4_gain), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/headphone_eq_b4_gain\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_headphone_eq_b5_gain), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/headphone_eq_b5_gain\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_fll_tuning), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/fll_tuning\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_dac_osr128), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/dac_osr128\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_adc_osr128), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/adc_osr128\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_dac_direct), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/dac_direct\n");
 		}
 		value = preferences.getInt(c.getString(R.string.key_mono_downmix), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/virtual/misc/scoobydoo_sound/mono_downmix\n");
 		}
 
 		// BLD
 		value = preferences.getInt(c.getString(R.string.key_bld_status), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/class/misc/backlightdimmer/enabled\n");
 			value = preferences.getInt(c.getString(R.string.key_bld_delay), -1);
 			if(value > -1)
 				command.append("echo " + value + " > " + "/sys/class/misc/backlightdimmer/delay\n");
 		}
 		
 		// BLN
 		value = preferences.getInt(c.getString(R.string.key_bln_status), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/class/misc/backlightnotification/enabled\n");
 			value = preferences.getInt(c.getString(R.string.key_bln_blink), -1);
 			if(value > -1)
 				command.append("echo " + value + " > " + "/sys/class/misc/backlightnotification/in_kernel_blink\n");
 			value = preferences.getInt(c.getString(R.string.key_bln_blink_interval), -1);
 			if(value > -1)
 				command.append("echo " + value + " > " + "/sys/class/misc/backlightnotification/blink_interval\n");
 			value = preferences.getInt(c.getString(R.string.key_bln_blink_count), -1);
 			if(value > -1)
 				command.append("echo " + value + " > " + "/sys/class/misc/backlightnotification/max_blink_count\n");
 		}
 
 		// BLX
 		value = preferences.getInt(c.getString(R.string.key_blx_charging_limit), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/class/misc/batterylifeextender/charging_limit\n");
 		}
 
 		//color
 		if(!ColorTuningPreference.isVoodoo()) { //no voodoo
         	for (String filePath : ColorTuningPreference.FILE_PATH) {
             	value = preferences.getInt(filePath, ColorTuningPreference.MAX_VALUE);
 		command.append("echo " + value + " > " + filePath + "\n");
         	}
         	for (String filePath : ColorTuningPreference.GAMMA_FILE_PATH) {
             	value = preferences.getInt(filePath, ColorTuningPreference.GAMMA_DEFAULT_VALUE);
 		command.append("echo " + value + " > " + filePath + "\n");
         	}
 		} else { // try voodoo
         	for (String filePath : ColorTuningPreference.VOODOO_FILE_PATH) {
             	value = preferences.getInt(filePath, ColorTuningPreference.MAX_VALUE);
 		command.append("echo " + value + " > " + filePath + "\n");
         	}
         	for (String filePath : ColorTuningPreference.VOODOO_GAMMA_FILE_PATH) {
             	value = (preferences.getInt(filePath, ColorTuningPreference.GAMMA_DEFAULT_VALUE) -40);
 		command.append("echo " + value + " > " + filePath + "\n");
         	}
 		}
 		// Mdnie
 		filepath = Mdnie.FILE;
 		value = Integer.parseInt(preferences.getString(filepath, "-1"));
 		if(value > -1) {
 			command.append("echo " + value + " > " + filepath + "\n");
 		}
 		
 		// Deepidle
 		value = preferences.getInt(c.getString(R.string.key_deepidle_status), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/class/misc/deepidle/enabled\n");
 		}
 
 
 		// Smooth Ui
 		value = preferences.getInt(c.getString(R.string.key_smooth_ui_enabled), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/class/misc/devil_tweaks/smooth_ui_enabled\n");
 		}
 
 		// Dyn Fsync
 		value = preferences.getInt(c.getString(R.string.key_dyn_fsync), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/kernel/dyn_fsync/Dyn_fsync_active\n");
 		}
 
 		// vibrator
 		value = preferences.getInt(c.getString(R.string.key_vibration_intensity), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/class/misc/pwm_duty/pwm_duty\n");
 		}
 		
 		// Touchwake
 		value = preferences.getInt(c.getString(R.string.key_touchwake_status), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/class/misc/touchwake/enabled\n");
 			value = preferences.getInt(c.getString(R.string.key_touchwake_delay), -1);
 			if(value > -1)
 				command.append("echo " + value + " > " + "/sys/class/misc/touchwake/delay\n");
 		}
 
 		// TouchBoost
 		value = preferences.getInt(c.getString(R.string.key_touch_boost_enabled), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/class/misc/touchboost/input_boost_enabled\n");
			value = preferences.getInt(c.getString(R.string.key_touch_boost_freq), -1);
 			if(value > -1)
 				command.append("echo " + value + " > " + "/sys/class/misc/touchboost/input_boost_freq\n");
 		}
 
 		// bigmem
 		value = Integer.parseInt(preferences.getString(c.getString(R.string.key_bigmem), "-1"));
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/kernel/bigmem/enable\n");
 		}
 		
 		// governor
 		status = preferences.getString(c.getString(R.string.key_governor), "-1");
 		if(!status.equals("-1")) {
 			command.append("echo " + status + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor\n");
 		}
 		// governor's parameters
 		if(status.equals("lazy")) { // set this parameter only if active governor = lazy
 			// lazy screenoff max freq
 			value = preferences.getInt(c.getString(R.string.key_screenoff_maxfreq), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lazy/screenoff_maxfreq\n");
 			}
 		}else if(status.equals("ondemand")) { // set this parameter only if active governor = ondemand
 			value = preferences.getInt(c.getString(R.string.key_ondemand_sampling_rate), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/ondemand/sampling_rate\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_ondemand_up_threshold), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/ondemand/up_threshold\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_ondemand_sampling_down_factor), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/ondemand/sampling_down_factor\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_ondemand_powersave_bias), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/ondemand/powersave_bias\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_ondemand_early_demand), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/ondemand/early_demand\n");
 				if (value == 1) {
 				value = preferences.getInt(c.getString(R.string.key_ondemand_grad_up_threshold), -1);
 				   if(value > -1) {
 					command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/ondemand/grad_up_threshold\n");
 				   }
 				}	
 			}
 			value = preferences.getInt(c.getString(R.string.key_ondemand_sleep_up_threshold), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/ondemand/sleep_up_threshold\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_ondemand_sleep_sampling_rate), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/ondemand/sleep_sampling_rate\n");
 			}
 
 		}else if(status.equals("conservative")) { // set this parameter only if active governor = conservative
 		    value = preferences.getInt(c.getString(R.string.key_conservative_sampling_rate), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/conservative/sampling_rate\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_conservative_down_threshold), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/conservative/down_threshold\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_conservative_up_threshold), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/conservative/up_threshold\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_conservative_sampling_down_factor), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/conservative/sampling_down_factor\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_conservative_freq_step), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/conservative/freq_step\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_conservative_ignore_nice_load), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/conservative/ignore_nice_load\n");
 			}
 
 			value = preferences.getInt(c.getString(R.string.key_conservative_smooth_up_enabled), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/conservative/smooth_up_enabled\n");
 				if (value == 1) {
 				value = preferences.getInt(c.getString(R.string.key_conservative_smooth_up), -1);
 				   if(value > -1) {
 					command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/conservative/smooth_up\n");
 				   }
 				}	
 			}
 			value = preferences.getInt(c.getString(R.string.key_conservative_sleep_up_threshold), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/conservative/sleep_up_threshold\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_conservative_sleep_sampling_rate), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/conservative/sleep_sampling_rate\n");
 			}
 
 		}else if(status.equals("smartassV2")) { // set this parameter only if active governor = smartass2
 			value = preferences.getInt(c.getString(R.string.key_smartass_awake_ideal_freq), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/smartass/awake_ideal_freq\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_smartass_sleep_ideal_freq), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/smartass/sleep_ideal_freq\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_smartass_sleep_wakeup_freq), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/smartass/sleep_wakeup_freq\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_smartass_min_cpu_load), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/smartass/min_cpu_load\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_smartass_max_cpu_load), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/smartass/max_cpu_load\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_smartass_ramp_down_step), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/smartass/ramp_down_step\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_smartass_ramp_up_step), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/smartass/ramp_up_step\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_smartass_down_rate_us), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/smartass/down_rate_us\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_smartass_up_rate_us), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/smartass/up_rate_us\n");
 			}
 		}else if(status.equals("interactive")) { // set this parameter only if active governor = interactive
 			value = preferences.getInt(c.getString(R.string.key_interactive_go_hispeed_load), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/interactive/go_hispeed_load\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_interactive_target_loads), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/interactive/target_loads\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_interactive_hispeed_freq), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/interactive/hispeed_freq\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_interactive_min_sample_time), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/interactive/min_sample_time\n");
 			}
 			value = preferences.getInt(c.getString(R.string.key_interactive_timer_rate), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/interactive/timer_rate\n");
 			}
 		}else if(status.equals("lulzactive")) { // set this parameter only if active governor = lulzactive
 			// lulzactive inc_cpu_load
 			value = preferences.getInt(c.getString(R.string.key_lulzactive_inc_cpu_load), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactive/inc_cpu_load\n");
 			}
 	
 			// lulzactive pump_up_step
 			value = preferences.getInt(c.getString(R.string.key_lulzactive_pump_up_step), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactive/pump_up_step\n");
 			}
 	
 			// lulzactive pump_down_step
 			value = preferences.getInt(c.getString(R.string.key_lulzactive_pump_down_step), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactive/pump_down_step\n");
 			}
 			
 			// lulzactive screen_off_min_step
 			value = preferences.getInt(c.getString(R.string.key_lulzactive_screen_off_min_step), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactive/screen_off_min_step\n");
 			}
 			
 			// lulzactive up_sample_time
 			value = preferences.getInt(c.getString(R.string.key_lulzactive_up_sample_time), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactive/up_sample_time\n");
 			}
 			
 			// lulzactive down_sample_time
 			value = preferences.getInt(c.getString(R.string.key_lulzactive_down_sample_time), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactive/down_sample_time\n");
 			}
 		}else if(status.equals("lulzactiveq")) { // set this parameter only if active governor = lulzactiveq
 			// lulzactiveq hispeed_freq
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hispeed_freq), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hispeed_freq\n");
 			}
 
 			// lulzactiveq inc_cpu_load
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_inc_cpu_load), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/inc_cpu_load\n");
 			}
 	
 			// lulzactiveq pump_up_step
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_pump_up_step), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/pump_up_step\n");
 			}
 	
 			// lulzactiveq pump_down_step
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_pump_down_step), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/pump_down_step\n");
 			}
 			
 			// lulzactiveq screen_off_max_step
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_screen_off_max_step), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/screen_off_max_step\n");
 			}
 			
 			// lulzactiveq up_sample_time
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_up_sample_time), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/up_sample_time\n");
 			}
 			
 			// lulzactiveq down_sample_time
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_down_sample_time), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/down_sample_time\n");
 			}
 
 			// lulzactiveq cpu_up_rate
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_cpu_up_rate), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/cpu_up_rate\n");
 			}
 
 			// lulzactiveq cpu_down_rate
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_cpu_down_rate), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/cpu_down_rate\n");
 			}
 
 			// lulzactiveq ignore_nice_load
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_ignore_nice_load), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/ignore_nice_load\n");
 			}
 
 			// lulzactiveq max_cpu_lock
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_max_cpu_lock), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/max_cpu_lock\n");
 			}
 
 			// lulzactiveq min_cpu_lock
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_min_cpu_lock), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/min_cpu_lock\n");
 			}
 
 			// lulzactiveq hotplug_freq_1_1
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hotplug_freq_1_1), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hotplug_freq_1_1\n");
 			}
 
 			// lulzactiveq hotplug_freq_2_0
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hotplug_freq_2_0), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hotplug_freq_2_0\n");
 			}
 
 			// lulzactiveq hotplug_freq_2_1
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hotplug_freq_2_1), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hotplug_freq_2_1\n");
 			}
 
 			// lulzactiveq hotplug_freq_3_0
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hotplug_freq_3_0), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hotplug_freq_3_0\n");
 			}
 
 			// lulzactiveq hotplug_freq_3_1
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hotplug_freq_3_1), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hotplug_freq_3_1\n");
 			}
 
 			// lulzactiveq hotplug_freq_4_0
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hotplug_freq_4_0), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hotplug_freq_4_0\n");
 			}
 
 			// lulzactiveq hotplug_rq_1_1
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hotplug_rq_1_1), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hotplug_rq_1_1\n");
 			}
 
 			// lulzactiveq hotplug_rq_2_0
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hotplug_rq_2_0), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hotplug_rq_2_0\n");
 			}
 
 			// lulzactiveq hotplug_rq_2_1
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hotplug_rq_2_1), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hotplug_rq_2_1\n");
 			}
 
 			// lulzactiveq hotplug_rq_3_0
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hotplug_rq_3_0), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hotplug_rq_3_0\n");
 			}
 
 			// lulzactiveq hotplug_rq_3_1
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hotplug_rq_3_1), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hotplug_rq_3_1\n");
 			}
 
 			// lulzactiveq hotplug_rq_4_0
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hotplug_rq_4_0), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hotplug_rq_4_0\n");
 			}
 
 			// lulzactiveq hotplug_sampling_rate
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hotplug_sampling_rate), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hotplug_sampling_rate\n");
 			}
 
 			// lulzactiveq up_nr_cpus
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_up_nr_cpus), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/up_nr_cpus\n");
 			}
 
 			// lulzactiveq hotplug_lock
 			value = preferences.getInt(c.getString(R.string.key_lulzactiveq_hotplug_lock), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lulzactiveq/hotplug_lock\n");
 			}
 
 		}else if(status.equals("hotplug")) { // set this parameter only if active governor = hotplug
 			value = preferences.getInt(c.getString(R.string.key_hotplug_up_threshold), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/hotplug/up_threshold\n");
 			}
 
 			value = preferences.getInt(c.getString(R.string.key_hotplug_down_threshold), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/hotplug/down_threshold\n");
 			}
 
 			value = preferences.getInt(c.getString(R.string.key_hotplug_sampling_rate), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/hotplug/sampling_rate\n");
 			}
 
 			value = preferences.getInt(c.getString(R.string.key_hotplug_ignore_nice_load), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/hotplug/ignore_nice_load\n");
 			}
 
 			value = preferences.getInt(c.getString(R.string.key_hotplug_io_is_busy), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/hotplug/io_is_busy\n");
 			}
 
 			value = preferences.getInt(c.getString(R.string.key_hotplug_down_differential), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/hotplug/down_differential\n");
 			}
 
 			value = preferences.getInt(c.getString(R.string.key_hotplug_in_sampling_periods), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/hotplug/hotplug_in_sampling_periods\n");
 			}
 
 			value = preferences.getInt(c.getString(R.string.key_hotplug_out_sampling_periods), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/hotplug/hotplug_out_sampling_periods\n");
 			}
 		} else if(status.equals("devilq")) { // set this parameter only if active governor = devilq
 			// devilq inc_cpu_load
 			value = preferences.getInt(c.getString(R.string.key_devilq_inc_cpu_load), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/inc_cpu_load\n");
 			}
 
 			value = preferences.getInt(c.getString(R.string.key_devilq_dec_cpu_load), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/inc_dec_load\n");
 			}
 		
 			// devilq up_sample_time
 			value = preferences.getInt(c.getString(R.string.key_devilq_up_sample_time), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/up_sample_time\n");
 			}
 			
 			// devilq down_sample_time
 			value = preferences.getInt(c.getString(R.string.key_devilq_down_sample_time), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/down_sample_time\n");
 			}
 
 			// devilq cpu_up_rate
 			value = preferences.getInt(c.getString(R.string.key_devilq_cpu_up_rate), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/cpu_up_rate\n");
 			}
 
 			// devilq cpu_down_rate
 			value = preferences.getInt(c.getString(R.string.key_devilq_cpu_down_rate), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/cpu_down_rate\n");
 			}
 
 			value = preferences.getInt(c.getString(R.string.key_devilq_early_demand), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/early_demand\n");
 				if (value == 1) {
 				value = preferences.getInt(c.getString(R.string.key_devilq_grad_up_threshold), -1);
 				   if(value > -1) {
 					command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/grad_up_threshold\n");
 				   }
 				value = preferences.getInt(c.getString(R.string.key_devilq_grad_down_threshold), -1);
 				   if(value > -1) {
 					command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/grad_down_threshold\n");
 				   }
 				}	
 			}
 
 			// devilq ignore_nice_load
 			value = preferences.getInt(c.getString(R.string.key_devilq_ignore_nice_load), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/ignore_nice_load\n");
 			}
 
 			// devilq max_cpu_lock
 			value = preferences.getInt(c.getString(R.string.key_devilq_max_cpu_lock), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/max_cpu_lock\n");
 			}
 
 			// devilq min_cpu_lock
 			value = preferences.getInt(c.getString(R.string.key_devilq_min_cpu_lock), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/min_cpu_lock\n");
 			}
 
 			// devilq hotplug_freq_1_1
 			value = preferences.getInt(c.getString(R.string.key_devilq_hotplug_freq_1_1), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/hotplug_freq_1_1\n");
 			}
 
 			// devilq hotplug_freq_2_0
 			value = preferences.getInt(c.getString(R.string.key_devilq_hotplug_freq_2_0), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/hotplug_freq_2_0\n");
 			}
 
 			// devilq hotplug_freq_2_1
 			value = preferences.getInt(c.getString(R.string.key_devilq_hotplug_freq_2_1), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/hotplug_freq_2_1\n");
 			}
 
 			// devilq hotplug_freq_3_0
 			value = preferences.getInt(c.getString(R.string.key_devilq_hotplug_freq_3_0), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/hotplug_freq_3_0\n");
 			}
 
 			// devilq hotplug_freq_3_1
 			value = preferences.getInt(c.getString(R.string.key_devilq_hotplug_freq_3_1), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/hotplug_freq_3_1\n");
 			}
 
 			// devilq hotplug_freq_4_0
 			value = preferences.getInt(c.getString(R.string.key_devilq_hotplug_freq_4_0), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/hotplug_freq_4_0\n");
 			}
 
 			// devilq hotplug_rq_1_1
 			value = preferences.getInt(c.getString(R.string.key_devilq_hotplug_rq_1_1), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/hotplug_rq_1_1\n");
 			}
 
 			// devilq hotplug_rq_2_0
 			value = preferences.getInt(c.getString(R.string.key_devilq_hotplug_rq_2_0), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/hotplug_rq_2_0\n");
 			}
 
 			// devilq hotplug_rq_2_1
 			value = preferences.getInt(c.getString(R.string.key_devilq_hotplug_rq_2_1), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/hotplug_rq_2_1\n");
 			}
 
 			// devilq hotplug_rq_3_0
 			value = preferences.getInt(c.getString(R.string.key_devilq_hotplug_rq_3_0), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/hotplug_rq_3_0\n");
 			}
 
 			// devilq hotplug_rq_3_1
 			value = preferences.getInt(c.getString(R.string.key_devilq_hotplug_rq_3_1), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/hotplug_rq_3_1\n");
 			}
 
 			// devilq hotplug_rq_4_0
 			value = preferences.getInt(c.getString(R.string.key_devilq_hotplug_rq_4_0), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/hotplug_rq_4_0\n");
 			}
 
 			// devilq hotplug_sampling_rate
 			value = preferences.getInt(c.getString(R.string.key_devilq_hotplug_sampling_rate), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/hotplug_sampling_rate\n");
 			}
 
 			// devilq up_nr_cpus
 			value = preferences.getInt(c.getString(R.string.key_devilq_up_nr_cpus), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/up_nr_cpus\n");
 			}
 
 			// devilq hotplug_lock
 			value = preferences.getInt(c.getString(R.string.key_devilq_hotplug_lock), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/hotplug_lock\n");
 			}
 
 			value = preferences.getInt(c.getString(R.string.key_devilq_suspend_max_cpu), -1);
 			if(value > -1) {
 				command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/devilq/suspend_max_cpu\n");
 			}
 		}
 
 		// battery
 		value = preferences.getInt(c.getString(R.string.key_dcp_ac_input_curr), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/platform/samsung-battery/dcp_ac_input_curr\n");
 		}
 
 		value = preferences.getInt(c.getString(R.string.key_dcp_ac_chrg_curr), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/platform/samsung-battery/dcp_ac_chrg_curr\n");
 		}
 
 		value = preferences.getInt(c.getString(R.string.key_sdp_input_curr), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/platform/samsung-battery/sdp_input_curr\n");
 		}
 
 		value = preferences.getInt(c.getString(R.string.key_sdp_chrg_curr), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/platform/samsung-battery/sdp_chrg_curr\n");
 		}
 
 		value = preferences.getInt(c.getString(R.string.key_cdp_input_curr), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/platform/samsung-battery/cdp_input_curr\n");
 		}
 
 		value = preferences.getInt(c.getString(R.string.key_cdp_chrg_curr), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/platform/samsung-battery/cdp_chrg_curr\n");
 		}
 
 		value = preferences.getInt(c.getString(R.string.key_batt_chrg_soft_volt), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/platform/samsung-battery/batt_chrg_soft_volt\n");
 		}
 
 		value = preferences.getInt(c.getString(R.string.key_ignore_stable_margin), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/platform/samsung-battery/ignore_stable_margin\n");
 		}
 
 		value = preferences.getInt(c.getString(R.string.key_ignore_unstable_power), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/devices/platform/samsung-battery/ignore_unstable_power\n");
 		}
 
 
 		// cmled
 		value = preferences.getInt(c.getString(R.string.key_cmled_bltimeout), -1);
 		if(value > -1) {
 			command.append("echo " + value + " > " + "/sys/class/misc/notification/bl_timeout\n");
 		}
 		// cmled blink
 		value = preferences.getInt(c.getString(R.string.key_cmled_blink), -1);
 		if(value > -1) {
 			// we must write blinktimeout before blink status
 			// coz if we write blinktimeout it will reset blink status to enabled
 			int timeout = preferences.getInt(c.getString(R.string.key_cmled_blinktimeout), -1);
 			if(timeout > -1)
 				command.append("echo " + timeout + ">" + "/sys/class/misc/notification/blinktimeout\n");
 			
 			command.append("echo " + value + " > " + "/sys/class/misc/notification/blink\n");
 		}
 
 		// gpu
         	for (String filePath : GpuFragment.GPU_CLOCK_FILE_PATH) {
             	status = preferences.getString(filePath, "-1");
 		if(!status.equals("-1"))
 		command.append("echo " + status + " > " + filePath + "\n");
         	}
 
 		status = preferences.getString(c.getString(R.string.key_malivolt_pref), "-1");
 		if(!status.equals("-1")) {
 			command.append("echo " + status + " > " + "/sys/class/misc/mali_control/voltage_control\n");
 		}
 		
 		// io scheduler
 		status = preferences.getString(c.getString(R.string.key_iosched), "-1");
 		if(!status.equals("-1")) {
 			String[] ioscheds = c.getResources().getStringArray(R.array.iosched_interfaces);
 			for(String i: ioscheds) {
 				command.append("echo " + status + " > " + i + "\n");	
 			}
 		}
 		
 		// Liveoc target low
 		int ocTargetLow = preferences.getInt(c.getString(R.string.key_liveoc_target_low), -1);
 		if(ocTargetLow > -1) {
 			command.append("echo " + ocTargetLow + " > " + "/sys/class/misc/liveoc/oc_target_low\n");
 		}
 		// Liveoc target high
 		int ocTargetHigh = preferences.getInt(c.getString(R.string.key_liveoc_target_high), -1);
 		if(ocTargetHigh > -1) {
 			command.append("echo " + ocTargetHigh + " > " + "/sys/class/misc/liveoc/oc_target_high\n");
 		}
 		// Liveoc
 		value = preferences.getInt(c.getString(R.string.key_liveoc), -1);
 		if(value > -1 && value != 100) {
 			// first make sure live oc at 100 then set cpu min and max freq
 			command.append("echo 100 > " + "/sys/class/misc/liveoc/oc_value\n");
 			// cpu minfreq
 			int minFreq = preferences.getInt(c.getString(R.string.key_min_cpufreq), -1);
 			if(minFreq > -1) {
 				if(minFreq >= ocTargetLow) {
 					minFreq = minFreq * 100 / value;
 				}
 				command.append("echo " + minFreq + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq\n");
 			}
 			// cpu maxfreq
 			int maxFreq = preferences.getInt(c.getString(R.string.key_max_cpufreq), -1);
 			if(maxFreq > -1) {
 				if(minFreq <= ocTargetHigh) {
 					maxFreq = maxFreq * 100 / value;
 				}
 				command.append("echo " + maxFreq + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq\n");
 			}
 			// now set liveoc value
 			command.append("echo " + value + " > " + "/sys/class/misc/liveoc/oc_value\n");
 		}else {
 			// make sure liveoc at 100
 			command.append("echo 100 > " + "/sys/class/misc/liveoc/oc_value\n");
 			// cpu minfreq
 			int minFreq = preferences.getInt(c.getString(R.string.key_min_cpufreq), -1);
 			if(minFreq > -1) {
 				command.append("echo " + minFreq + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq\n");
 			}
 			
 			// cpu maxfreq
 			int maxFreq = preferences.getInt(c.getString(R.string.key_max_cpufreq), -1);
 			if(maxFreq > -1) {
 				command.append("echo " + maxFreq + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq\n");
 			}
 		}
 		return command.toString();
 	}
 	
 	public static void deleteSettings(Context c, String preferenceName) {
 		File destination = new File(c.getString(R.string.SETTINGS_DIR), preferenceName);
 		destination.delete();
 	}
 	
 	public static boolean saveSettings(Context c, String preferenceName) {
 		boolean ret = false;
 		File destDir = new File(c.getString(R.string.SETTINGS_DIR));
 		if(!destDir.exists())
 			destDir.mkdirs(); // create dir if not exists
 		File destination = new File(destDir, preferenceName);
 
 		if(!destination.exists()) {
 			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
 	
 			String command = buildCommand(c, preferences);
 			
 			try {
 				FileWriter fw = new FileWriter(destination);
 				fw.write("# v" + c.getString(R.string.app_version) + "\n");
 				fw.write(command);
 				fw.close();
 				ret = true;
 			}catch(IOException e) {
 				Log.e(LOG_TAG, "", e);
 			}
 		}
 		return ret;
 	}
 	
 	private static void checkOldSavedFile(Context c, File source) {
 		try {
 			FileReader fr = new FileReader(source);
 			BufferedReader br = new BufferedReader(fr);
 			String line = br.readLine();
 			String lastLine = null;
 			String versionString = "# v" + c.getString(R.string.app_version);
 			StringBuilder command = new StringBuilder(versionString);
 			command.append("\n");
 			boolean rewrite = false;
 			if(line != null && !line.equals(versionString)) {
 				rewrite = true;
 				do {
 					if(line.contains("scaling_min_freq") && !lastLine.contains("oc_value")) {
 						command.append("echo 100 > " + "/sys/class/misc/liveoc/oc_value\n");
 					}
 					if(line != null) {
 						command.append(line);
 						command.append("\n");
 					}
 					lastLine = line;
 					line = br.readLine();
 				}while(line != null);
 			}
 			br.close();
 			fr.close();
 			if(rewrite) {
 				FileWriter fw = new FileWriter(source);
 				fw.write(command.toString());
 				fw.close();
 			}
 		}catch(Exception ex) {
 			Log.e(LOG_TAG, "", ex);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param c
 	 * @param preferenceName
 	 * @return
 	 */
 	public static void loadSettings(Context c, String preferenceName) {
 		File source = new File(c.getString(R.string.SETTINGS_DIR), preferenceName);
 		checkOldSavedFile(c, source); // check old saved file
 		StringBuilder command = new StringBuilder();
 		try {
 			FileReader fr = new FileReader(source);
 			BufferedReader br = new BufferedReader(fr);
 			String line = br.readLine();
 			while(line != null) {
 				command.append(line);
 				command.append("\n");
 				line = br.readLine();
 			}
 			br.close();
 			fr.close();
 		}catch(IOException e) {
 			Log.e(LOG_TAG, "", e);
 		}
 		if(command.length() > 0) {
 			SysCommand.getInstance().suRun(command.toString());
 		}
 	}
 	
 	/**
 	 * this method called on boot completed
 	 * 
 	 * @param c
 	 * @return
 	 */
 	public static int loadSettingsOnBoot(Context c) {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
 		
 		// check 'set on boot' preference
 		boolean restoreOnBoot = preferences.getBoolean(c.getString(R.string.key_restore_on_boot), true);
 		boolean forceRestore = preferences.getBoolean(c.getString(R.string.key_force_restore_on_boot), false);
 		if(!restoreOnBoot) {
 			return ERR_SET_ON_BOOT_FALSE;
 		}
 
 		// now check current kernel version with saved value
 		restoreOnBoot = false;
 		SysCommand sysCommand = SysCommand.getInstance();
 		if(sysCommand.readSysfs("/proc/version") > 0) {
 			String kernel = sysCommand.getLastResult(0);
 			String savedKernelVersion = preferences.getString(c.getString(R.string.key_kernel_version), null);
 			if(kernel.equals(savedKernelVersion) || forceRestore) {
 				restoreOnBoot = true;
 			}
 		}
 		if(!restoreOnBoot) {
 			return ERR_DIFFERENT_KERNEL;
 		}
 		boolean restoreOnInitd = preferences.getBoolean(c.getString(R.string.key_restore_on_initd), false);
 		if(!restoreOnInitd) {
 			String command = buildCommand(c, preferences);
 			SysCommand.getInstance().suRun(command);
         		ColorTuningPreference.restore(c);
         		Mdnie.restore(c);
 		}
 		return SUCCESS;
 	}
 	
 	public static void saveToInitd(Context c) {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
 		boolean restoreOnBoot = preferences.getBoolean(c.getString(R.string.key_restore_on_boot), true);
 		boolean restoreOnInitd = preferences.getBoolean(c.getString(R.string.key_restore_on_initd), false);
 		boolean forceRestore = preferences.getBoolean(c.getString(R.string.key_force_restore_on_boot), false);
 		SysCommand sysCommand = SysCommand.getInstance();
 		if(restoreOnBoot && restoreOnInitd) {
 			Log.d(LOG_TAG, "write to init.d script");
 			StringBuilder cmd = new StringBuilder();
 			cmd.append("mount -o remount,rw /dev/block/platform/s3c-sdhci.0/by-name/system /system\n");
 			cmd.append("echo a > " + c.getString(R.string.INITD_SCRIPT) + "\n");
 			cmd.append("chmod 0777 " + c.getString(R.string.INITD_SCRIPT) + "\n");
 			sysCommand.suRun(cmd.toString());
 			
 			File destination = new File(c.getString(R.string.INITD_SCRIPT));
 			String command = buildCommand(c, preferences);
 			try {
 				FileWriter fw = new FileWriter(destination);
 				fw.write("#!/system/bin/sh\n");
 				fw.write("CUR=`cat /proc/version`\n");
 				fw.write("SAV=\""+preferences.getString(c.getString(R.string.key_kernel_version), null)+"\"\n");
 				if (!forceRestore) {
 				fw.write("if [ ! \"$CUR\" == \"$SAV\" ] ; then\n");
 				fw.write("exit\n");
 				fw.write("fi\n");
 				}
 				fw.write(command);
 				fw.close();
 			}catch(IOException e) {
 				Log.e(LOG_TAG, "", e);
 			}
 			sysCommand.suRun("mount", "-o", "remount,ro", "/dev/block/platform/s3c-sdhci.0/by-name/system", "/system");
 		}else {
 			Log.d(LOG_TAG, "remove init.d script");
 			StringBuilder cmd = new StringBuilder();
 			cmd.append("mount -o remount,rw /dev/block/platform/s3c-sdhci.0/by-name/system /system\n");
 			cmd.append("rm " + c.getString(R.string.INITD_SCRIPT) + "\n");
 			cmd.append("mount -o remount,ro /dev/block/platform/s3c-sdhci.0/by-name/system /system\n");
 			sysCommand.suRun(cmd.toString());
 		}
 	}
 }
