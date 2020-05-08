 package tool;
 
 //import sun.net.dns.ResolverConfiguration.Options;
 import commons.konoha2.*;
 
 import java.io.*;
 import java.net.CacheRequest;
 import org.apache.commons.cli.*;
 
 public class Konoha {
 	
 	public static int compileonlyFlag = 0;       // global variable
 	public static int interactiveFlag = 0;       // global variable
 	
	public static Options longOptions;
//	public static int verboseDebug = 0;	         //global variable
//	public static int verboseGc	= 0;	         //global variable
//	public static int verboseSugar = 0;	         //global variable
//	public static int verboseCode = 0;	         //global variable
 
 	public static String startupScript = null;	 //global variable
 	public static String builtinTest = null;	 //global variable
 	public static String testScript = null;	     //global variable
 	
 	// src/konoha/methods.h
 	public static int assertResult = 0;
 	
 	
 	
 //	private static Writer stdlog; // only used in test.
 //	public static final int K_PAGESIZE = 4096;// MACRO
 
 	
 	
 /*	public static String optarg; //TODO import gnu.getopt.Getopt; in ginit()
 	public static int optind; //TODO import gnu.getopt.Getopt; in ginit();
 	//TODO make long_options(struct)
 	private static String[] long_options = { "verbose", "verbose:gc", "verbose:sugar", 
 								"verbose:code", "interactive", "typecheck", 
 								"start-with", "test", "test-with", "builtin-test", "NULL" };
 */	
 	static {
 		longOptions = new Options();
 		longOptions.addOption(null, "verbose", false, null);
 		longOptions.addOption(null, "verbose:gc", false, null);
 		longOptions.addOption(null, "verbose:sugar", false, null);
 		longOptions.addOption(null, "verbose:code", false, null);
 		longOptions.addOption("i", "interactive", false, null);
 		longOptions.addOption("c", "typecheck", false, null);
 		longOptions.addOption("S", "start-with", true, null);
 		longOptions.addOption("T", "test", true, null);
 		longOptions.addOption("T", "test-with", true, null);
 		longOptions.addOption("B", "builtin-test", true, null);
 	}
 	
 	public static void main(String[] args) throws IOException {
 		boolean ret = true;
 		int scriptidx = ginit(args);
 		if(builtinTest != null) {
 			System.exit(builtinTest(builtinTest));
 		}
 		if(testScript != null) {
 			System.exit(test(testScript));
 		}
 		CTX konoha = open();
 		if(startupScript != null) {
 			startup(konoha, startupScript);
 		}
 		if(scriptidx < args.length) {
 			ret = load(konoha, args[scriptidx]);
 		}
 		if(ret && (interactiveFlag != 0)) {    // TODO interactiveFlag to boolean?
 			ret = kShell(konoha);
 		}
 		close(konoha);
 		// MODGC_check_malloced_size()
 		System.exit(ret ? assertResult : 1);
 	}
 
 	public static int ginit(String[] args) {
 		if (System.getenv("KONOHA_DEBUG") != null) {
 			longOptions.getOption("verbose");
 			verboseDebug = 1;
 			verboseGc = 1;
 			verboseSugar = 1;
 			verboseCode = 1;
 		}
 		//TODO Getopt options = new Getopt(long_options/*TODO long_options(struct)*/, args, "icI:S:");
 		//TODO import gnu.getopt.Getopt;
 		while(true) {
 			int optionIndex = 0;
 			int c = optionIndex; //TODO options.getopt();
 			if (c == -1) break; /* Detect the end of the options. */
 			switch (c) {
 			case 0://TODO
 				/* If this option set a flag, do nothing else now. */
 			case 'c':
 				compileonlyFlag = 1;
 				break;
 			case 'i':
 				interactiveFlag = 1;
 				break;
 			case 'B':
 				builtinTest = optarg;
 				break;
 			case 'S':
 				startupScript = optarg;
 				break;
 			case 'T':
 				testScript = optarg;
 				break;
 			case '?':
 				break;
 			default:
 			//CacheRequest.abort(); TODO needs override?
 			}
 		}
 		if (optind < args.length) {
 			interactiveFlag = 1;
 		}
 		return optind;
 	}
 	
 	
 	public static int builtinTest(String name) {//TODO How to do #ifdef, #else, #endif
 	/*
 	#ifdef USE_BUILTINTEST
 		Ftest f = lookupTestFunc(KonohaTestSet, name);
 		if(f != NULL) {
 			konoha_t konoha = konoha_open();
 			int ret = f((CTX_t)konoha);
 			konoha_close(konoha);
 			return ret;
 		}
 		fprintf(stderr, "Built-in test is not found: '%s'\n", name);
 	#else
 		fprintf(stderr, "Built-in tests are not built; rebuild with -DUSE_BUILTINTEST\n");
 	#endif
 		return 1;
 	*/
 		try {
 		FileWriter stderr = new FileWriter("./stderr");
 		stderr.write("Built-in tests are not built; rebuild with -DUSE_BUILTINTEST" + name);
 		} catch (IOException e) {
 			System.out.println(e + "Exception occured");
 		}
 		return 1;
 	}
 	
 	public static int test(String testname) throws IOException {
 		//reduced error message
 		verboseDebug	= 0;
 		verboseSugar	= 0;
 		verboseGc 		= 0;
 		verboseCode	= 0;
 		CTX konoha = new CTX();//TODO konoha_t(CTX) konoha = konoha_open();
 		if (startupScript != null) {
 			startup (konoha, startupScript);
 		}
 		int ret = 0;//OK
 		String scriptFile = new String(testname);
 		String correctFile = new String(scriptFile);
 		String resultFile = new String(scriptFile);
 		
 		FileReader fp = new FileReader(correctFile);
 		stdlog.write(resultFile);
 		//TODO ((struct _klib2*)konoha->lib2)->Kreport  = kreport;
 		//TODO ((struct _klib2*)konoha->lib2)->Kreportf = kreportf;
 		load(konoha, "Q.E.D.\n");//Q.E.D.
 		stdlog.close();
 		if (fp != null) { //TODO Not need?
 		FileReader fp2 = new FileReader (resultFile);
 		ret = checkResult(fp, fp2);
 		if (ret == 0)
 			stdout.write("[PASS]:" + testname + "\n");//TODO How to do stdout?
 		fp.close();
 		fp2.close();
 		} else {
 			ret = 1;
 		}
 		konohaClose(konoha);//TODO
 		return ret;
 	}
 	public void kreport (CTX _ctx, String msg) throws IOException {//used in test
 		stdlog.flush ();
 		stdlog.write("-");
 		stdlog.write(msg);
 		stdlog.write("\n");
 	}
 	public void kreportf (CTX _ctx, int level,/*TODO kline_t pline,*/String fmt) throws IOException {
 		//used in test
 		if (level == 5/*TODO DEBUG_*/ && !(verboseSugar == 0) ) return;
 		/*va_list ap; TODO
 		va_start(ap , fmt);*/
 		
 		stdlog.flush ();
 		/* TODO
 		if (pline != 0) {
 			String file = T_file(pline);
 			stdlog.write (" - (" + shortName(file) + ":" + (kushort_t)pline + ")" + T_ERR(level) );
 		} else {
 			stdlog.write(" - " + T_ERR(level) );
 		}
 		*/
 		stdlog.write(fmt);
 		stdlog.write("\n");
 		
 		//va_end(ap);
 		if (level == 0/*TODO CRIT_*/) {
 			//TODO kraise(0);
 		}
 	}
 	public static int checkResult(FileReader fp0, FileReader fp1) {//used in test. Compare fp0 between fp1
 		//TODO
 		if ( fp0.equals(fp1) )
 			return 0;
 		else
 			return 1;//FAILED
 	}
 	public static CTX open() {
 		init();
 		return new_context (null, K_PAGESIZE * 8);
 	}
 	public static void init() {//used in open()
 		int isInit = 0;
 		if (isInit == 0)
 			isInit = 1;
 	}
 	public static CTX new_context (CTX _ctx, int stacksize) {//used in open()
 		CTX newctx = new CTX ();
 		int ctxid_counter = 0;
 		ctxid_counter++;
 		if (_ctx == null) {//null means first one.
 			/*TODO 	struct _klib2 *klib2 = (struct _klib2*)calloc(sizeof(klib2_t) + sizeof(kcontext_t), 1);
 			 */
 			_klib2 klib2 = new _klib2();
 			klib2_init(klib2);//TODO klib2_init method(function)
 			newctx = (CTX)(klib2 + 1);
 			newctx.lib2 = (CTX)klib2;
 			_ctx = newctx;
 			/*newctx->modshare = (kmodshare_t**)calloc(sizeof(kmodshare_t*), MOD_MAX);
 			newctx->modlocal = (kmodlocal_t**)calloc(sizeof(kmodlocal_t*), MOD_MAX);
 			
 			//TODO MODLOGGER_init method, MODGC_init method, KCLASSTABLE_init method
 			MODLOGGER_init(_ctx, newctx);
 			MODGC_init(_ctx, newctx);
 			KCLASSTABLE_init(_ctx, newctx);*/
 			
 		} else {//others take ctx as its parent
 			newctx.lib2 = _ctx.lib2;
 			newctx.memshare = _ctx.memshare;
 			newctx.share = _ctx.share;
 			newctx.modshare = _ctx.modshare;
 			newctx.modlocal = _ctx.modlocal;
 		}
 		//TODO porting the method
 		/*KRUNTIME_init(_ctx, newctx, stacksize);
 		if (IS_ROOTCTX(newctx)) {
 			MODCODE_init(_ctx, newctx);
 			MODSUGAR_init(_ctx, newctx);
 			KCLASSTABLE_loadMethod(_ctx);
 			MODSUGAR_loadMethod(_ctx)
 		}*/
 		return newctx;
 	}
 
 	public static void startup(CTX konoha, String startup_script) {
 		String[] buf = new String[256];
 		String path = System.getenv("KONOHA_SCRIPTPATH");
 		String local = "";
 		if (path == null) {
 			path = System.getenv("KONOHA_HOME");
 			local = "/script";
 		}	
 		if (path == null) {
 			path = System.getenv("HOME");
 			local = "/.konoha/script";
 		}
 		//TODO snprintf(buf, sizeof(buf), "%s%s%s.k", path, local, startup_script);
 		if (true /*TODO load(konoha, (const char*)buf)*/ ) {
 			System.exit(1);
 		}
 	}
 
 	/*	public static String readline(String prompt)
 	{
 		static int checkCTL = 0;
 		int ch, pos = 0;
 		static StringBuffer linebuf = new StringBuffer(1024); // THREAD-UNSAFE
 		FileWriter fw = new FileWriter(stdout);
 		fw.write(prompt);
 		BufferedReader br = new BufferedReader(new FileReader(stdin));
 		while((ch = br.read()) != -1) { //TODO while(... != EOF)
 			if(ch == '\r') continue;
 			if(ch == 27) {
 				 ^[[A ;
 				br.read(); br.read();
 				if(checkCTL == 0) {
 					fr.write(" - use readline, it provides better shell experience.\n");
 					checkCTL = 1;
 				}
 				continue;
 			}
 			if(ch == '\n' || pos == linebuf.length() - 1) {
 				linebuf.append(0);
 				break;
 			}
 			linebuf.append(ch);
 			pos++;
 		}
 		if(ch == -1) return null; //TODO if(ch == EOF)
 		String p = new String(linebuf);
 		return p;
 	}
 
 	static int add_history(String line)
 	{
 		return 0;
 	}*/
 
 	static void shell(CTX ctx)
 	{
 		kwb_t wb;
 		kwb_init((ctx.stack.cwb), wb);
 		kline_t uline/* = FILEID_("(shell)") | 1*/;
 		while(true) {
 			kline_t inc = 0;
 			kstatus_t status = readstmt(ctx, wb, inc);
 			if(status == K_CONTINUE && kwb_bytesize(wb) > 0) {
 				status = konoha_eval(ctx, kwb_top(wb, 1), uline);
 				uline += inc;
 				kwb_free(wb);
 				if(status != K_FAILED) {
 					dumpEval(ctx, wb);
 					kwb_free(wb);
 				}
 			}
 			if(status == K_BREAK) {
 				break;
 			}
 		}
 		kwb_free(wb);
 		FileWriter fw = new FileWriter(stdout);
 		fw.write(stdout + "\n");
 		return;
 	}
 
 	static void show_version(CTX ctx)
 	{
 		int i;
 		FileWriter fw = new FileWriter(stdout);
 		fw.write("Konoha 2.0-alpha (Miyajima) (" + K_REVISION + "," + __DATE__ + ")\n");
 		fw.write("[gcc " + __VERSxION__ + "]\n");
 		fw.write("options:");
 		for(i = 0; i < MOD_MAX; i++) {
 			if(ctx.modshare[i] != null) {
 				fw.write(ctx.modshare[i].name);
 			}
 		}
 		fw.write("\n");
 		fw.close();
 	}
 
 	public static boolean kShell(CTX konoha) { //"shell" is already exist
 		void *handler = dlopen("libreadline" K_OSDLLEXT, RTLD_LAZY);
 		void *f = (handler != NULL) ? dlsym(handler, "readline") : null;
 		kreadline = (f != NULL) ? (char* (*)(const char*))f : readline;
 		f = (handler != NULL) ? dlsym(handler, "add_history") : null;
 		kadd_history = (f != NULL) ? (int (*)(const char*))f : add_history;
 		show_version((CTX_t)konoha);
 		shell((CTX_t)konoha);//"shell" is here
 		return 1;
 	}
 
 	public static void close(CTX konoha) {
 		kcontext_free(konoha, (kcontext_t*)konoha);
 	}
 
 }
