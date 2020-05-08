 /*
  *  Copyright 2013-2014 Jeroen Gorter <Lowerland@hotmail.com>
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package nl.dreamkernel.s4.tweaker.systeminfo;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import nl.dreamkernel.s4.tweaker.R;
 import nl.dreamkernel.s4.tweaker.util.RootProcess;
 import nl.dreamkernel.s4.tweaker.util.SysCmds;
 import nl.dreamkernel.s4.tweaker.util.SysFs;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 public class SysInfo extends Activity {
 	static final String TAG = "S4Tweaker";
 
 	public static TextView Kernel_Version;
 
 	// Cpu Info
 	public static TextView Processor;
 	public static TextView BogoMIPS;
 	public static TextView Features;
 	public static TextView CPU_implementer;
 	public static TextView CPU_architecture;
 	public static TextView CPU_variant;
 	public static TextView CPU_part;
 	public static TextView CPU_revision;
 	public static TextView Hardware;
 	public static TextView Revision;
 	public static TextView Serial;
 
 	public static TextView kernel_cmdline;
 
 	public static final SysFs vCheck_Kernel_CMDLine = new SysFs("/proc/cmdline");
 	// public static final SysFs vCheck_Kernel_CMDLine = new
 	// SysFs("/mnt/sdcard/testfiles/cmdline");
 
 	public static String Kernel_CMDLine_temp;
 	public static String Kernel_CMDLine;
 	public static final String NoInfo = "No Info";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.systeminfo);
 		setTitle(R.string.menu_system_info);
 		getActionBar().hide();
 
 		// Kernel Version
 		Kernel_Version = (TextView) findViewById(R.id.kernel_version);
 
 		// Cpu Info
 		Processor = (TextView) findViewById(R.id.Processor);
 		BogoMIPS = (TextView) findViewById(R.id.BogoMIPS);
 		Features = (TextView) findViewById(R.id.Features);
 		CPU_implementer = (TextView) findViewById(R.id.CPU_implementer);
 		CPU_architecture = (TextView) findViewById(R.id.CPU_architecture);
 		CPU_variant = (TextView) findViewById(R.id.CPU_variant);
 		CPU_part = (TextView) findViewById(R.id.CPU_part);
 		CPU_revision = (TextView) findViewById(R.id.CPU_revision);
 		Hardware = (TextView) findViewById(R.id.Hardware);
 		Revision = (TextView) findViewById(R.id.Revision);
 		Serial = (TextView) findViewById(R.id.Serial);
 
 		// Kernel Commandline
 		kernel_cmdline = (TextView) findViewById(R.id.kernel_cmdline);
 
 		Processor.setText(SysCmds.CPUinfo("Processor"));
 		BogoMIPS.setText(SysCmds.CPUinfo("BogoMIPS"));
 		Features.setText(SysCmds.CPUinfo("Features"));
 		CPU_implementer.setText(SysCmds.CPUinfo("CPU implementer"));
 		CPU_architecture.setText(SysCmds.CPUinfo("CPU architecture"));
 		CPU_variant.setText(SysCmds.CPUinfo("CPU variant"));
 		CPU_part.setText(SysCmds.CPUinfo("CPU part"));
 		CPU_revision.setText(SysCmds.CPUinfo("CPU revision"));
 		Hardware.setText(SysCmds.CPUinfo("Hardware"));
 		Revision.setText(SysCmds.CPUinfo("Revision"));
 		Serial.setText(SysCmds.CPUinfo("Serial"));
 
 		Kernel_Version.setText(getKernelVersion());
 		kernel_cmdline.setText(getKernelcmdline() + "\n");
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 
 	public static String kv;
 
 	public static String getKernelVersion() {
 		String ret = SysCmds.kernelversion("");
 		if (SysInfo.isNullOfEmpty(ret)) {
 			return NoInfo;
 		} else {
			if (ret.indexOf('-') > 0) {
				Pattern p = Pattern.compile("\\(((.*)\\))");
 				Matcher m = p.matcher(ret);
 				if (m.find()) {
 					kv = m.toMatchResult().group(0);
					kv = kv.replace(" (gcc", "gcc").replace("(", "")
							.replace(")", "\n").replace("GCC", "");
 					return kv;
 				}
 			} else {
 				return NoInfo;
 			}
 		}
 		return ret;
 	}
 
 	public static String kc;
 	public static String ret2;
 
 	public static String getKernelcmdline() {
 
 		if (vCheck_Kernel_CMDLine.exists()) {
 			RootProcess rootProcess = new RootProcess();
 			rootProcess.init();
 			ret2 = vCheck_Kernel_CMDLine.read(rootProcess);
 			rootProcess.term();
 			if (SysInfo.isNullOfEmpty(ret2)) {
 				return NoInfo;
 			} else {
 				if (ret2.indexOf(" ") > -1) {
 					Pattern o = Pattern.compile("^((.*))");
 					Matcher h = o.matcher(ret2);
 					if (h.find()) {
 						kc = h.toMatchResult().group(0);
 						kc = kc.replace(" ", "\n");
 						return kc;
 					}
 				}
 			}
 		} else {
 			return NoInfo;
 		}
 		return ret2;
 	}
 
 	public static boolean isNullOfEmpty(String value) {
 		return (value == null || "".equals(value));
 	}
 
 	public static void sleep(long msec) {
 		try {
 			Thread.sleep(60);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
