 package jp.co.omega11.webcrawler.w2fj;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import jp.co.omega11.universal.controller.receivecommand.ReceiveCommandThread;
 import jp.co.omega11.universal.controller.receivecommand.component.console.ControlFromConsole;
 import jp.co.omega11.universal.controller.receivecommand.component.mail.ControlFromMail;
 import jp.co.omega11.universal.util.log.Loger;
 import jp.co.omega11.webcrawler.w2fj.component.contents.ContentsDownloderThread;
 import jp.co.omega11.webcrawler.w2fj.component.dat.DatDownloderThread;
 import jp.co.omega11.webcrawler.w2fj.component.remotecontrol.ExecuteCommandForW2fj;
 import jp.co.omega11.webcrawler.w2fj.component.subject.SubjectTextGet;
 import jp.co.omega11.webcrawler.w2fj.component.subject.SubjectTextGetThread;
 import jp.co.omega11.webcrawler.w2fj.model.systemInfomation.RootInfo;
 import jp.co.omega11.webcrawler.w2fj.set.Setting;
 
 /**
  * W2FJ[Watch 2ch For Java]ׂ̂ĂǗ郋[gNX
  * ̃NXł͂PłȂ
  * ̔ꍇÃNX𕡐т悤ĂяoNXŎ
  * ۂ̑zł11vZXƂĎAeXgsĂB
  *
  * 1vZXŕ̃NX΂Ă邱Ƃ͏Iɂ͂Ȃݒt@C̋LqɂĂ
  * ԂdȂďubLO肷\͂B
  * ܂o̓tH_dȂ̂œ𓯎ɕ΂ȂƁiNXbh𐧌΃ubLO͂Ȃj
  *
  * @author Wizard1
  *
  */
 public class W2fjMainLogic {
 
 	/**
 	 * RAƂȂXbhNXQ
 	 * Xbh~AċN𐧌䂷邽߃tB[hɂ
 	 */
 	private SubjectTextGetThread subjectGetThread;
 	private DatDownloderThread datDownloderThread;
 	private ContentsDownloderThread contentsDownloderThread;
 	private List <ReceiveCommandThread> receiveCommandThreads = new ArrayList<ReceiveCommandThread>();
 
 	// falsel
 	private boolean subjectGetThread_ExeFlag;
 	private boolean datDownloderThread_ExeFlag;
 	private boolean contentsDownloderThread_ExeFlag;
 	private boolean noThread;
 
 	// R}ht@\̃XbhsۃtO
	// TODO Ƃ肠True
	private boolean controlFromConsole_ExeFlag = true;
 	private boolean controlFromMail_ExeFlag;
 
 	public W2fjMainLogic() {
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * N\bh
 	 * @param args
 	 */
 	public void start(String args[]) {
 		/**
 		 * XbhRg[CNX eXbh͐ݒ肳ꂽԊԊuŋN eXbh͓Ƃ炸ꂼDBlďs
 		 * ܂eXbh͓ƗēĂ菇Ԃ͊{Iɑ݂Ȃ
 		 *
 		 * RlNV͊eXbhłPÂێ
 		 *
 		 *@rȈAُIADB̃JoƂ@\͌ォǉƂ
 		 *
 		 * }`vbgtH[ΏƂƂ Windows Linux MacOSX Ńob`Ƃēł悤
 		 * DB\tg̊œ̂^[QbgƂ }`vbgtH[œȂJAVA Cu͎gȂ
 		 * GUIEclipseGUI or QT or SWTg
 		 *
 		 * R[h fBNgAt@CȂǂ̃pX
 		 *
 		 * Ȃǂ͒ӂ
 		 *
 		 * .NETł悤JAVAŗL̏͂Ȃׂ Interfacełi`u`NX𒊏ۉăC^[tF[X\bhgp
 		 *
 		 * DB
 		 * Apache Derby
 		 * MySQl
 		 * PostgreSQL
 		 * Oracle11g
 		 * http://ja.wikipedia.org/wiki/SQLite
 		 * PlainFile
 		 *
 		 *
 		 * eXbh͕ʃ}VłNł悤ɂ U
 		 *
 		 *
 		 * XbhZ[t\bhANXǂCɂčĂȂ̂ŌŐ
 		 *
 		 * g WebHyperSplider悤 Amazon̉摜WƂAbv[_[N[[Ƃ
 		 *
 		 * ^[Qbg̓j[X񂨂+ 
 		 *
 		 * [eXg{]
 		 * EDBT[o[st@Cƕʒ[ɂẲ^p
 		 * ERlNV̕ێ̗L
 		 *
 		 * [A]]
 		 * EOXg[Wɉ摜]łIvV
 		 *   DAOŁ@X^[gŏI܂łIĂ̊Ԃ̉摜Xg[WhCu̎w肳ꂽtH_ɂ
 		 *   @dԂƂłoroŉ摜mFł̂ŕ֗
 		 *   @摜̃TCY͌ォΉ
 		 * Ê߂ɂ݂͌Ăt@Cnh̖Kv
 		 * E񃂃f͂newiP̃XbĥƂ̃Xbh擾łȂ悤Ɂj
 		 * ERemoteP̋N̂ƂĩXbh͕ʃvZXjǂďƂ邩
 		 * EPOPY[f[g[eXg]
 		 * ENԂx点@ŋNҋ@Ԃ𕪁AԂŐݒł悤(ԃ[eKv)
 		 * EIMAP
 		 * EÔƂɔԂƈ̕\
 		 * ESQL̃oChϐ̃O\
 		 * Ejavadoc̐
 		 * EfvC@\AIAbvf[g
 		 * Eꗗ̎擾
 		 * E2ch̏Ԃ`FbN邽ߐڑeXg ping̓[^[ł͂邱Ƃ̂ŕs
 		 * Et@C擾AsAȂǂ̃MO
 		 * ET[o[Ǘ@\
 		 * Efth
 		 * E^XN̎ԂL^ăOt@\[ł悤
 		 * EQ UserOC@\
 		 * EtqktB^[@uN@\
 		 * Etqkd̔r
 		 * E悤ׁAjRjRΉ
 		 * Egnrs}X^e[uWebN[[̂
 		 * EJDBCRlNṼJo
 		 * Em[h_E[h@\
 		 * ERec_E[_[̕XbhNǗ
 		 * EvZXw2fjNƂ̏WǗ@\[GUI/PHP? ƂɂPNX͕Kv]
 		 * ERlNV}l[W[(getconnectionNXŊǗAcaPANZXłȂƂcaQփANZX悤ɂ)
 		 * @̂DB1ƂcaQ̓͂`oł͕ۏ؂ȂA`oœƂ悤ȏɓdANZXIvV͕Kv
 		 * EOVXeւDBR[h̃Rs[iAPT[o[OJ邽߂ɕKv,FTPSQL𑗐MEEȂ)
 		 *  	傩ȃVXeł`ôPHP/RoRō\z
 		 * ECELL TV݂ɃReciXjƂɉ摜ł悤ɂJavaFX
 		 *
 		 *
 		 * [ς]
 		 *@E
 		 *  E
 		 * [eXgς]
 		 * E2chƂv`hs邾ŗȂ
 		 * E4TԂ̘Aғ
 		 * EdꂽAċNNĂ㑱sƁB^ǂcaANZXĂ^C~OłȂ
 		 *
 		 */
 
 		Setting set = new Setting();
 
 
 
 		// p[XĐݒt@CI
 		argumentParser(args, set);
 
 		try {
 			set.setInitialize(set);
 		} catch (Exception e) {
 			Loger.print("s");
 			Loger.print(e);
 			return;
 		}
 
 		RootInfo rootInfo = new RootInfo(this.getClass().getSimpleName(), set.getItaname());
 
 		if (subjectGetThread_ExeFlag) {
 			// subject.txtGet XbhN subject.txt_E[hăp[XXbhDBo^
 			subjectGetThread = new SubjectTextGetThread(set, rootInfo.getSubjectInfo());
 			subjectGetThread.start();
 		}
 
 		if (subjectGetThread_ExeFlag && datDownloderThread_ExeFlag) {
 
 			// e[u̐60b܂
 			try {
 				Thread.sleep(60000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				Loger.print(e);
 			}
 		}
 
 		if (datDownloderThread_ExeFlag) {
 			// DAT_E[hXbhNXbhtableĕKvȂ̂_E[h摜URLo摜DBo^܂
 			// DatDownloader
 			datDownloderThread = new DatDownloderThread(set , rootInfo.getDatInfo());
 			datDownloderThread.start();
 		}
 
 		if (contentsDownloderThread_ExeFlag) {
 			// 摜_E[hXbhN摜tableā摜_E[hDB̃R[h_E[hς݂UPDATE
 			// ContentsDownloader
 			contentsDownloderThread = new ContentsDownloderThread(set , rootInfo.getContentsInfo());
 			contentsDownloderThread.start();
 		}
 
 		// ORg[ R\[MailȂ[\]
 		if (controlFromConsole_ExeFlag) {
 			receiveCommandThreads.add (new ReceiveCommandThread(
 					new ControlFromConsole(), new ExecuteCommandForW2fj(rootInfo)));
 		}
 
 		if(controlFromMail_ExeFlag) {
 
 			//[MŕKvȏ
 			set.setRemoteControlForMail();
 
 			receiveCommandThreads.add (new ReceiveCommandThread(
 					new ControlFromMail(set.getPop3Model(), set.getSmtpModel(), set.getMailSleepTime()) , new ExecuteCommandForW2fj(rootInfo)));
 		}
 
 
 		for (ReceiveCommandThread receiveCommandThread : receiveCommandThreads) {
 			receiveCommandThread.start();
 		}
 
 		// cãob` 폜tOR[h̗e[uւ̈ڍs
 		// DBBatchLogic
 
 	}
 
 	/**
 	 * R}hCp[XĐݒIuWFNg̃ftHgl㏑܂
 	 * ݒׂ͂XMLt@Cōs߂Őݒł̂͐ݒt@Ĉ
 	 *
 	 * Ƃ͋NXbhIł悤ɂ
 	 *
 	 * @param args
 	 * @param set
 	 */
 	private void argumentParser(String args[], Setting set) {
 		/**
 		 * NŋNXbh肷
 		 *
 		 * -S Subjecttext̂
 		 * -D DatDownloder̂
 		 * -C ContentDownloder̂
 		 *
 		 * -S -D (-S/-D̂)
 		 * -ALL ftHgAׂċN
 		 *
 		 * -NT No Thread ifobOpANvZXM@\fobOƂɎgpj
 		 * 	   XbhR|[lgN܂
 		 *
 		 * -F ݒwlkt@Cw
 		 *
 		 * -O O@\w肵܂(\)
 		 *    -Console
 		 *    -Mail
 		 *    -Gui
 		 *    -TCP [TCP Server]
 		 *
 		 *
 		 * -Re Reboot ċN܂@VKvZXN@݃vZXI܂
 		 * -WS WaitStart NJn܂ł̃EFCgbw肵܂@ċNp
 		 * -Shutdown I܂
 		 * -fexit Forse Exit I܂
 		 *
 		 */
 		for(int i=0;i<args.length;i++){
 			if("-F".equals(args[i])){
 				set.setSettingFilename(args[i+1]);
 				// ݒt@C̈Ȃ̂ŉZ
 				i++;
 			}
 
 			if("-S".equals(args[i])) {
 				Loger.print("-SEEsubjectGetThreadNONɂ܂");
 				subjectGetThread_ExeFlag = true;
 			}
 
 			if("-D".equals(args[i])) {
 				Loger.print("-DEEdatDownloderThread_ExeFlagNONɂ܂");
 				datDownloderThread_ExeFlag = true;
 			}
 
 			if("-C".equals(args[i])) {
 				Loger.print("-CEEcontentsDownloderThread_ExeFlagNONɂ܂");
 				contentsDownloderThread_ExeFlag = true;
 			}
 
 			if("-NP".equals(args[i])) {
 				Loger.print("-NPEENoParseExe URLoDBo^񃋁[vɎs܂");
 				set.setNoParseExe(true);
 			}
 
 
 			if("-NT".equals(args[i])) {
 				Loger.print("-NTEENoThread XbhR|[lgN܂BvZX̂ݎ{");
 				noThread = true;
 			}
 
 
 			if("-O".equals(args[i])){
 
 				if("-Console".equals(args[i+1])){
 					controlFromConsole_ExeFlag = true;
 				}
 				else if ("-Mail".equals(args[i+1])) {
 					/**
 					 * Mail̎M@\
 					 * [̌ɔ
 					 * {ɃR}hi[ƕԐM܂
 					 */
 					controlFromMail_ExeFlag = true;
 				}
 
 				// ݒt@C̈Ȃ̂ŉZ
 				i++;
 			}
 
 		}
 
 		if (subjectGetThread_ExeFlag == false &&
 				datDownloderThread_ExeFlag == false &&
 				contentsDownloderThread_ExeFlag == false &&
 				noThread == false) {
 			// ׂfalsȅꍇ͋NXbhw薳= ALLNƂ݂Ȃ
 
 			Loger.print("NXbhw肳ĂȂ߁AׂċN܂B");
 			subjectGetThread_ExeFlag = true;
 			datDownloderThread_ExeFlag = true;
 			contentsDownloderThread_ExeFlag = true;
 		}
 
 	}
 
 	/**
 	 * GUI @[R}hŊOXbh~߂鎞Ɏgp
 	 * @param className@
 	 */
 	public void threadStop(String className){
 		if(className.equals(SubjectTextGet.class.getCanonicalName())){
 			// TODO XbhŗÕLb`䂪Kv
 			subjectGetThread.interrupt();
 		}
 
 	}
 
 
 	public void threadReStart(){
 
 	}
 }
