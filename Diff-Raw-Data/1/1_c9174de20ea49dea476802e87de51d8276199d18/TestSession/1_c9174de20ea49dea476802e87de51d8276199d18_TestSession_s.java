 package reporting_tool;
 
 import java.util.ArrayList;
 import java.util.ListIterator;
 
 public class TestSession {
 
 	class DateType {
 		private  Integer year;
 		private Integer month;
 		private Integer day;
 		private Integer hour;
 		private Integer min;
 		private Integer sec;
 
 		public DateType(Integer d,Integer m,Integer y,Integer h,Integer min,Integer sec) {
 			this.year=y;
 			this.month=m;
 			this.day=d;
 			this.hour=h;
 			this.min=min;
 			this.sec=sec;
 		}
 
 		String print_date() {
 			return  String.format("%2d/%2d/%2d",month,day,year);
 		}
 
 		String print_time() {
 			return  String.format("%2d:%2d:%2d",hour,min,sec);
 		}
 	}
 
 	private String empty="(none)";
 	private String target_name;
 	private String size_target;
 	private String Cur_bench_name;
 	private String Cur_phase_name;
 	private ArrayList<Test>  obj_size_tests; 
 	private ArrayList<Test>  bin_size_tests; 
 	private ArrayList<Test>  func_size_tests; 
 	private ArrayList<Test>  appli_size_tests; 
 	private ArrayList<Test>  speed_tests; 
 	private boolean extension_built;
 	public String name;
 	public String path;
 	public String mode;
 	public String compiler_name;
 	public String compiler_version;
 	public String compiler_date;
 	public String toolst_path;
 	public String simulator_version;
 	public String executor_name;
 	public String logdir;
 	public String build_number;
 	public ArrayList<String>  compiler_flags;
 	public ArrayList<String>  simulator_flags;
 	public ArrayList<String>  failures;
 	public DateType date; 
 
 	private String section_name(Sections sec) {
 		switch(sec){
 		case TEXT: 			return ".text";
 		case DATA: 			return ".data";
 		case RODATA: 			return ".rodata";
 		case BSS: 			return ".bss";
 		case SYMTAB: 			return ".symtab";
 		case STRTAB: 			return ".strtab";
 		case DBG_SECTION: 		return ".debug_section";
 		case RELA_TEXT: 		return ".rela.text";
 		case RELA_DATA: 		return ".rela.data";
 		case RELA_RODATA: 		return ".rela.rodata";
 		case RELA_DBG_FRAME: 		return ".rela.debug_frame";
 		case STXP70_EXTRECONF_INFO: 	return ".STXP70_EXTRECONF_INFO";
 		case STXP70_MEMINFO: 		return ".STXP70_MEMINFO";
 		case STXP70_MEMMAP_INFO: 	return ".STXP70_MEMMAP_INFO";
 		case STARTUP: 		return ".startup";
 		case THANDLERS: 		return ".thandler";
 		case STACK1: 			return ".stack1";
 		case HEAP: 			return ".heap";
 		case DA: 			return ".da";
 		case TDA: 			return ".tda";
 		case SDA: 			return ".sda";
 		case SECINIT: 		return ".secinit";
 		case SHSRTAB: 		return ".shsrtab";
 		case TDA_DATA: 		return ".tda_data";
 		case TDA_BSS: 		return ".tda_bss";
 		case TDA_RO:   		return ".tda_ro";
 		case SDA_DATA: 		return ".sda_data";
 		case SDA_BSS:  		return ".sda_bss";
 		case SDA_RO:   		return ".sda_ro";
 		case DA_DATA:  		return ".da_data";
 		case DA_BSS:   		return ".da_bss";
 		case DA_RO:    		return ".da_bss";
 		case SBSS:   			return ".sbss";
 		case SYSCALL:  		return ".syscall";
 		case ITHANDLERS: 		return ".ithandler";
 		case IVTABLE: 		return ".ivtable";
 		case ROBASE: 			return ".robase";
 		case TOTAL: 			return ".TOTAL";
 		case RELA_TDA_DATA:   	return ".rela.tda_data";
 		case RELA_TDA_BSS:    	return ".rela.tda_bss";
 		case RELA_TDA_RO:     	return ".rela.tda_ro";
 		case RELA_SDA_DATA:   	return ".rela.sda_data";
 		case RELA_SDA_BSS:    	return ".rela.sda_bss";
 		case RELA_SDA_RO:     	return ".rela.sda_ro";
 		case RELA_DA_DATA:    	return ".rela.da_data";
 		case RELA_DA_BSS:     	return ".rela.da_bss";
 		case RELA_DA_RO:      	return ".rela.da_bss";
 		default: 			return "unknown";
 		}
 	}
 
 	private Test find_test(String name, Discriminent disc) {
 		for (int i=0; i<get_tests(disc).size();i++) {
 			if (get_tests(disc).get(i).name.contentEquals(name)) 
 				return get_tests(disc).get(i);
 		}
 		return null;
 	}
 
 	public TestSession(String path, String name) {
 		obj_size_tests=new ArrayList<Test>();
 		bin_size_tests=new ArrayList<Test>();
 		func_size_tests=new ArrayList<Test>();
 		appli_size_tests=new ArrayList<Test>();
 		speed_tests=new ArrayList<Test>();
 		compiler_flags=new ArrayList<String>();
 		simulator_flags=new ArrayList<String>();
 		failures=new ArrayList<String>();
 		this.path=path;
 		this.name=name;
 		extension_built=false;
 		simulator_version=null;
 		compiler_version=null;
 		Cur_bench_name=null;
 		Cur_phase_name=null;
 	}
 
 	public ArrayList<Test> get_tests(Discriminent disc) {
 		switch (disc) {
 		case SIZE_OBJ: return obj_size_tests; 
 		case SIZE_BIN: return bin_size_tests; 
 		case SIZE_FUNC: return func_size_tests;
 		case SIZE_APPLI: return appli_size_tests;
 		case SPEED: return speed_tests; 
 		default: return null;
 		}
 	}
 
 	public void ignore_flag(String string) {
 		if (compiler_flags.contains(string)) {
 			if (sqa_report.warn_level > 0) System.err.println("WARNING: Option " + string + " ignored\n");
 			compiler_flags.remove(string);
 		}
 	}
 
 	public void ignore_all_flags() {
 		if (sqa_report.warn_level > 0) System.err.println("WARNING: All Option are ignored\n");
 		if(!compiler_flags.isEmpty()) compiler_flags.clear();
 	}
 
 	public void add_test_size(String test_name, String target, Sections sec, Integer size, Discriminent disc) {
 		Target mytarget;
 		if (Cur_phase_name != null && Cur_phase_name.contentEquals("PFO_Phase_1_Instrumentation")){
 			if (sqa_report.warn_level > 0) System.err.println("INFO: Size count for PFO_Phase_1_Instrumentation ignored\n"); 
 			return;
 		}
 		if (Cur_phase_name != null && Cur_phase_name.contentEquals("FDO_Phase_1_Instrumentation")){
 			if (sqa_report.warn_level > 0) System.err.println("INFO: Size count for FDO_Phase_1_Instrumentation ignored\n"); 
 			return;
 		}
 		if(target.contains( "Perfs_BILBO.o")) return;
 		if(target.contains( "Verbose.o")) return;
 		if(target.contains( "Check.o")) return;
 		if(target.startsWith("./")) target.replace("./","");
 
 		String local_test_name = compute_size_test_target_name(test_name.toLowerCase(),target);
 
 		Test current_test=null;
 		for (int i=0; i<get_tests(disc).size();i++) {
 			if(get_tests(disc).get(i).name.contentEquals(local_test_name)) {
 				current_test=get_tests(disc).get(i);
 				break;
 			}
 		}
 		if (current_test == null) {
 			add_test_name(local_test_name);
 			add_test_size(local_test_name, size_target, sec, size, disc);
 			return;
 		}
 
 		mytarget = current_test.find_target(size_target);
 		if (mytarget==null) {
 			mytarget = current_test.add_target(size_target);
 		}
 
 		Integer current_tgt_size = mytarget.get_tgt_size(sec);  		
 		if (current_tgt_size!=0 && size == current_tgt_size ) {
 			//Size already given => Skip it
 			return;
 		}
 		if (current_tgt_size!=0) {
 			if(sqa_report.warn_level >= 2) 
 				System.err.println("WARNING: Size value for test "+ test_name + " target " + target + " section " + section_name(sec) + " already given\n");
 		}
 		mytarget.set_size(sec, current_tgt_size + size);
 	}
 
 	public void add_test_func_size(String test_name, String target, String object,	String function, int size, boolean aggregated) {
 		Target mytarget;
 		Discriminent disc=Discriminent.SIZE_FUNC;
 		String my_string_target = target;
 		if(function.contains("TH_InitTimer")) return;
 		if(function.contains("TH_GetOverhead")) return;
 		if(function.contains("TH_end")) return;
 		if(function.contains("BENCH_START")) return;
 		if(function.contains("BENCH_STOP")) return;
 		if(function.contains("BENCH_STATUS")) return;
 		if(function.contains("BENCH_INT_MESSAGE")) return;
 		if(function.contains("BENCH_FLOAT_MESSAGE")) return;
 		if(function.contains("BENCH_STR_MESSAGE")) return;
 		if(function.contains("BENCH_CHECK")) return;
 
 		/* PStone;auto/ auto/src/auto.o autom 4550 4194532*/
 		/* PStone;auto/ auto/src/auto.o 4188 autom */
 		/* Test Name : <test_name>_<target>
 		 * Target :  <function> (<object>)
 		 */
 
 		if(object.contains(".ipakeep") && object.contains("..")) {
 			object = object.substring(0,object.lastIndexOf(".."));
 		}
 
 		String tmp_test_name = compute_size_test_target_name(test_name.toLowerCase(),object);
 		Test current_test=null;
 		String local_test_name; 
 		if (my_string_target.contains("src/")) {
 			local_test_name=tmp_test_name;
 		} else {
 			if(my_string_target.endsWith("/")) 	my_string_target = my_string_target.substring(0, my_string_target.length()-1);
 			String[] tmp_for_test = object.split("/");
 			if (tmp_test_name.contentEquals("eembc_consumer") && 
 					target.contains("filters") && object.contains("filters")) {
 				my_string_target = tmp_for_test[1];
 			} else {
 				for (int i=0; i<tmp_for_test.length-1;i++) {
 					if (tmp_for_test[i].contains(my_string_target) && !tmp_for_test[i].contentEquals(my_string_target)) {
 						my_string_target = tmp_for_test[i];
 						break;
 					}
 				}
 				if (object.contains("th_lite")) {
 					my_string_target = "lib" ;
 				}
 				my_string_target = my_string_target.replace(".o", "");
 				my_string_target = my_string_target.replace(".u.po.ipakeep", "");
 				my_string_target = my_string_target.replace("_lite", "");
 			}
 			local_test_name=tmp_test_name + " - " + my_string_target;
 		}
 
 		if (aggregated) 	target_name = function + "(Aggregated Object)";
 		else 				target_name = function + "(" + object + ")";
 
 		for (int i=0; i<get_tests(disc).size();i++) {
 			if(get_tests(disc).get(i).name.contentEquals(local_test_name)) {
 				current_test=get_tests(disc).get(i);
 				break;
 			}
 		}
 		if (current_test == null) {
 			add_test_name(local_test_name);
 			for (int i=0; i<get_tests(disc).size();i++) {
 				if(get_tests(disc).get(i).name.contentEquals(local_test_name)) {
 					current_test=get_tests(disc).get(i);
 					break;
 				}
 			}
 		}
 
 		mytarget = current_test.find_target(target_name);
 		if (mytarget==null) mytarget = current_test.add_target(target_name);
 
 		Integer current_tgt_fct_size=mytarget.get_tgt_size(Sections.TEXT);
 		if (current_tgt_fct_size!= 0 && aggregated) 	return;
 		mytarget.set_size(Sections.TEXT, size);
 
 		/*Add appli size*/
 		disc = Discriminent.SIZE_APPLI;
 		Test current_appli_test=null;
 		for (int i=0; i<get_tests(disc).size();i++) {
 			if(get_tests(disc).get(i).name.contentEquals(local_test_name)) {
 				current_appli_test=get_tests(disc).get(i);
 				break;
 			}
 		}
 		if (current_appli_test == null) {
 			System.err.println("ERROR: current test not found");
 		} else {
 			mytarget = current_appli_test.find_target("Full Application");
 			if (mytarget==null) mytarget = current_appli_test.add_target("Full Application");
 			Integer old_size = mytarget.get_tgt_size(Sections.TEXT); 
 			mytarget.set_size(Sections.TEXT, size + old_size);
 		}
 	}
 
 	private String compute_size_test_target_name(String test_name, String object) {
 		if (object.contains(".u") && object.contains("/")) 
 			size_target = object.substring(object.lastIndexOf("/")+1, object.length());
 		else 
 			size_target = object;
 		if(test_name.contains("eembc_v11.automotive"))		return "eembc_automotive";
 		if(test_name.contains("eembc_v11.consumer"))      	return "eembc_consumer";
 		if (test_name.contains( "eembc_v20.networking"))  	return "eembc_networking";
 		if (test_name.contains( "eembc_v11.telecom"))     	return "eembc_telecom";
 		return test_name;
 	}
 
 	public void add_cycle_count(String name, Long value) {
 		if (Cur_phase_name != null && Cur_phase_name.contentEquals("PFO_Phase_1_Instrumentation")){
 			if (sqa_report.warn_level > 0) System.err.println("INFO: Cycle count for PFO_Phase_1_Instrumentation ignored\n"); 
 			return;
 		}
 		if (Cur_phase_name != null && Cur_phase_name.contentEquals("FDO_Phase_1_Instrumentation")){
 			if (sqa_report.warn_level > 0) System.err.println("INFO: Cycle count for FDO_Phase_1_Instrumentation ignored\n"); 
 			return;
 		}
 		for (int i=0; i<speed_tests.size();i++) {
 			if (speed_tests.get(i).name.contentEquals(name.toLowerCase())) {
 				if (sqa_report.warn_level > 0) System.err.println("WARNING: Test suit " + name + " already given, skipped\n"); 
 				return;
 			}
 		}
 		String local_test_name = compute_test_and_target_name(name.toLowerCase());
 		Test mytest = find_test(local_test_name,Discriminent.SPEED);
 		if (mytest == null) mytest =  new Test(local_test_name);
 		speed_tests.add(mytest);
 		Target mytarget = mytest.find_target(target_name);
 		if (mytarget == null) mytarget=mytest.add_target(target_name);
 		if (mytarget.cycles != -1)  value += mytarget.cycles;
 		mytarget.cycles = value;
 	}
 
 	public void set_current_test_parsing(String bench_name, String phase_name) {
 		Cur_bench_name=bench_name;
 		Cur_phase_name=phase_name;
 	}
 	
 	private String compute_test_and_target_name(String name) {
 		if (name.contains("eembc_v")) {
 			target_name = name.substring(name.lastIndexOf("--")+2);
 			if (name.contains("consumer"))    return "eembc_consumer";
 			if (name.contains("networking"))  return "eembc_networking";
 			if (name.contains("telecom"))     return "eembc_telecom";
 			if (name.contains("automotive"))  return "eembc_automotive";
 		} 
 		if (name.contains("--")) {
 			target_name = name.substring(name.lastIndexOf("--")+2);
 			if (name.contains("lao-kernels")) {
 				if (target_name.contains(" ")) {
 					target_name = target_name.substring(0,target_name.lastIndexOf(" "));
 				}
 				return "lao-kernels";
 			}
 			if (name.contains("qmx_mixer")) {
 				target_name = target_name.substring(0, target_name.length()-6);
 				return "qmx_mixer";
 			}
 			if (name.contains("pstone") || name.contains("diagnostics") || name.contains("audio_speech")) {
 				if (name.contains("/")) 
 					target_name = target_name.substring(0,target_name.indexOf("/"));
 				return name.substring(0,name.indexOf("--"));
 			}
 			if (name.contains("src/")) {
 				target_name = target_name.substring(target_name.indexOf("src/")+4);
 				if (target_name.endsWith(".log")) target_name = target_name.replace(".log", ""); 
 				return name.substring(0,name.indexOf("--"));
 			}
 			if (target_name.endsWith(".log")) target_name = target_name.replace(".log", ""); 
 			return name.substring(0,name.indexOf("--"));
 		}
 		target_name = name;
 		return name;
 	}
 
 	public void add_test_name(String name) {
 		for (int i=0; i<obj_size_tests.size();i++) {
 			if (obj_size_tests.get(i).name.contentEquals(name)) {
 				if(sqa_report.warn_level > 0) System.err.printf("WARNING: Test suit %s already given, skipped\n",name);
 				return ;
 			}
 		}
 		if(name.contains("valid_extension") || name.contains("valid-extensions")) {
 			if(extension_built) return;
 			extension_built=true;
 			obj_size_tests.add(new Test("tstx_test"));
 			obj_size_tests.add(new Test("vx2_fgtdec"));
 			obj_size_tests.add(new Test("vx2_test_memspace"));
 			obj_size_tests.add(new Test("ts3x_test"));
 			obj_size_tests.add(new Test("admx_appliusingadmx"));
 			obj_size_tests.add(new Test("ts2x_test"));
 			obj_size_tests.add(new Test("qmx_farrow_interpolator"));
 			obj_size_tests.add(new Test("qmx_mixer"));
 			obj_size_tests.add(new Test("tx_csd_extension_test"));
 			obj_size_tests.add(new Test("mp1v_viterbi"));
 			obj_size_tests.add(new Test("fx_samplerate"));
 			obj_size_tests.add(new Test("ts4x_test"));
 
 			bin_size_tests.add(new Test("tstx_test"));
 			bin_size_tests.add(new Test("vx2_fgtdec"));
 			bin_size_tests.add(new Test("vx2_test_memspace"));
 			bin_size_tests.add(new Test("ts3x_test"));
 			bin_size_tests.add(new Test("admx_appliusingadmx"));
 			bin_size_tests.add(new Test("TS2x_test"));
 			bin_size_tests.add(new Test("qmx_farrow_interpolator"));
 			bin_size_tests.add(new Test("qmx_mixer"));
 			bin_size_tests.add(new Test("tx_csd_extension_test"));
 			bin_size_tests.add(new Test("mp1v_viterbi"));
 			bin_size_tests.add(new Test("fx_samplerate"));
 			bin_size_tests.add(new Test("ts4x_test"));
 
 			func_size_tests.add(new Test("tstx_test"));
 			func_size_tests.add(new Test("vx2_fgtdec"));
 			func_size_tests.add(new Test("vx2_test_memspace"));
 			func_size_tests.add(new Test("ts3x_test"));
 			func_size_tests.add(new Test("admx_appliusingadmx"));
 			func_size_tests.add(new Test("ts2x_test"));
 			func_size_tests.add(new Test("qmx_farrow_interpolator"));
 			func_size_tests.add(new Test("qmx_mixer"));
 			func_size_tests.add(new Test("tx_csd_extension_test"));
 			func_size_tests.add(new Test("mp1v_viterbi"));
 			func_size_tests.add(new Test("fx_samplerate"));
 			func_size_tests.add(new Test("ts4x_test"));
 
 			appli_size_tests.add(new Test("tstx_test"));
 			appli_size_tests.add(new Test("vx2_fgtdec"));
 			appli_size_tests.add(new Test("vx2_test_memspace"));
 			appli_size_tests.add(new Test("ts3x_test"));
 			appli_size_tests.add(new Test("admx_appliusingadmx"));
 			appli_size_tests.add(new Test("ts2x_test"));
 			appli_size_tests.add(new Test("qmx_farrow_interpolator"));
 			appli_size_tests.add(new Test("qmx_mixer"));
 			appli_size_tests.add(new Test("tx_csd_extension_test"));
 			appli_size_tests.add(new Test("mp1v_viterbi"));
 			appli_size_tests.add(new Test("fx_samplerate"));
 			appli_size_tests.add(new Test("ts4x_test"));
 			return;
 		}
 
 		if(name.contains("whetstone")){
 			obj_size_tests.add(new Test("whetstone-float"));
 			obj_size_tests.add(new Test("whetstone-double"));
 			bin_size_tests.add(new Test("whetstone-float"));
 			bin_size_tests.add(new Test("whetstone-double"));
 			func_size_tests.add(new Test("whetstone-float"));
 			func_size_tests.add(new Test("whetstone-double"));
 			appli_size_tests.add(new Test("whetstone-float"));
 			appli_size_tests.add(new Test("whetstone-double"));
 			return;
 		} 
 
 		obj_size_tests.add(new Test(name));
 		bin_size_tests.add(new Test(name));
 		func_size_tests.add(new Test(name));
 		appli_size_tests.add(new Test(name));
 	}
 
 	public void set_date(Integer day, Integer month, Integer year, Integer hour, Integer min, Integer sec) {
 		date = new DateType(day,month,year,hour,min,sec);
 	}
 
 	public String print_date() {
 		return date.print_date();
 	}
 	public String print_time() {
 		return date.print_time();
 	}
 
 
 	private boolean has_failed(String test_name, String target_name) {
 		ListIterator<String> iter = failures.listIterator();
 		while(iter.hasNext()) {
 			if (test_name.startsWith(iter.next())) return true;
 		}
 		return false;
 	}
 
 	public long get_cycle(RootTest tests) {
 		if (!CommonData.is_same_list(compiler_flags,tests.get_options()))  return -1;
 		Test mytst= find_test(tests.get_test(),Discriminent.SPEED);
 		if (mytst==null) {
 			if (has_failed(tests.get_test(),tests.get_target()))  return   CommonData.HAS_FAILED;
 			return -1;  
 		}
 		
 		Target mytg = mytst.find_target(tests.get_target());
 		if (mytg==null) {
 			if (has_failed(tests.get_test(),tests.get_target()))  return   CommonData.HAS_FAILED;
 			return -1;  
 		}
 		long value = mytg.get_cycle(); 
 		if (value <=0 && has_failed(mytst.name,mytg.get_name())) return   CommonData.HAS_FAILED;
 		return value;
 	}
 
 
 	public Integer get_size(RootTest tests, Sections sec, Discriminent disc, boolean monitor) {
 		ListIterator<Target> iter;
 		if (!CommonData.is_same_list(compiler_flags,tests.get_options())) return -1;
 		Test mytst= find_test(tests.get_test(), disc);
 		if (mytst==null) {
 			if (has_failed(tests.get_test(),tests.get_target()))  return   CommonData.HAS_FAILED;
 			return -1;  
 		}
 		if (monitor) {
 			Integer value=0;
 			if (tests.get_target().contentEquals(tests.get_test()) || tests.get_test().contentEquals("jpeg_csd")) {
 				//Test has only one target
 				iter = mytst.get_target().listIterator();
 				while(iter.hasNext()) {
 					value += iter.next().get_tgt_size(sec); 
 				}          
 				return value;
 			}
 			if (tests.get_test().contentEquals("audio_csd")) {
 				//Specific treatment for audio_csd
 				if(tests.get_target().contentEquals("Huffman decoding")) {
 					iter = mytst.get_target().listIterator();
 					while(iter.hasNext()) {
 						Target tgt = iter.next();
 						if (tgt.name.contains("huffmandecoding.o")) value += tgt.get_tgt_size(sec);
 						if (tgt.name.contains("bit_parse.o")) value += tgt.get_tgt_size(sec);
 					}
 					return value;
 				}
 				if(tests.get_target().contentEquals("IIR")) {
 					iter = mytst.get_target().listIterator();
 					while(iter.hasNext()) {
 						Target tgt = iter.next();
 						if (tgt.name.contains("iir.o")) return tgt.get_tgt_size(sec);
 					}           
 				}
 				if(tests.get_target().contentEquals("IFFT")) {
 					iter = mytst.get_target().listIterator();
 					while(iter.hasNext()) {
 						Target tgt = iter.next();
 						if (tgt.name.contains("ifft.o")) return tgt.get_tgt_size(sec);
 					}           
 				}
 				if(tests.get_target().contentEquals("IDCT")) {
 					iter = mytst.get_target().listIterator();
 					while(iter.hasNext()) {
 						Target tgt = iter.next();
 						if (tgt.name.contains("idct.o")) return tgt.get_tgt_size(sec);
 					}           
 				}
 				if(tests.get_target().contentEquals("SRC")) {
 					iter = mytst.get_target().listIterator();
 					while(iter.hasNext()) {
 						Target tgt = iter.next();
 						if (tgt.name.contains("oversampleinterpolate.o")) return tgt.get_tgt_size(sec);
 					}           
 				}
 				return CommonData.HAS_FAILED;
 			}      
 			if (tests.get_target().contentEquals("Total")) {
 				iter = mytst.get_target().listIterator();
 				while(iter.hasNext()) {
 					Target tgt = iter.next();
 					if (tgt.name.contains(tests.get_target())) {
 						value += tgt.get_tgt_size(sec);
 					}
 				}
 				return value;
 			}
 			iter = mytst.get_target().listIterator();
 			while(iter.hasNext()) {
 				Target tgt = iter.next();
 				if (tgt.name.contains(tests.get_target())) {
 					value += tgt.get_tgt_size(sec); 
 				}
 			}
 			return value;
 		} else {
 			Target mytg = mytst.find_target(tests.get_target());
 			if (mytg==null) {
 				if (has_failed(tests.get_test(),tests.get_target()))  return   CommonData.HAS_FAILED;
 				return -1;  
 			}
 			Integer value = mytg.get_tgt_size(sec); 
 			if (value <= 0 && has_failed(mytst.name,mytg.get_name())) return   CommonData.HAS_FAILED;
 			return value;
 		}
 	}
 
 	public String get_cc_ver() {
 		if (compiler_version==null || compiler_version.isEmpty())	return empty;
 		return compiler_version;
 	}
 
 	public String get_cc_date() {
 		if (compiler_date==null || compiler_date.isEmpty())	return empty;
 		return compiler_date;
 	}
 
 	public String get_sim_ver() {
 		if (simulator_version==null || simulator_version.isEmpty())	return empty;
 		return simulator_version;
 	}
 
 	public void set_compiler_name(String name) {
 		if (!sqa_report.Cruise_Control) {
 			compiler_name=name;
 			toolst_path=name;
 		}
 		else {
 			// /home/compwork/cruisecontrol/open64-nightly-res/compilers/open64-linux-dt25-dev-gcm-merge-4-2-23140/stxp70v4/toolset
 			toolst_path=name;
 			compiler_name = name.substring(name.indexOf("open64-linux")+13);
 			compiler_name = compiler_name.substring(0,compiler_name.indexOf("/"));
 		}
 		
 	}
 	
 	public void set_logdir_info(String node, String name) {
 		// (gnx5281) /tmp/open64-dt25-dev-aci-config-short_52/38936/stxp70v4/O3MultCASDual/aec16b
 		executor_name=node;
 		logdir=name;
 		build_number = name.substring(name.indexOf("open64-linux")+13);
 		build_number = build_number.substring(0,build_number.indexOf("/"));
 		build_number = build_number.substring(build_number.indexOf("_")+1);
 	}
 }
