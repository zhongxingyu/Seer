 package com.raygroupintl.m.token;
 
 import static org.junit.Assert.fail;
 import junit.framework.Assert;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.raygroupintl.m.token.MTFSupply;
 import com.raygroupintl.m.token.MVersion;
 import com.raygroupintl.m.token.TFCommand;
 import com.raygroupintl.parser.SyntaxErrorException;
 import com.raygroupintl.parser.Text;
 import com.raygroupintl.parser.Token;
 import com.raygroupintl.parser.TokenFactory;
 
 public class TFCommandTest {	
 	private static TFCommand fStd95;
 	private static TFCommand fCache;
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		MTFSupply tfsStd95 = MTFSupply.getInstance(MVersion.ANSI_STD_95);
 		fStd95 = tfsStd95.command;
 		
 		MTFSupply tfsCache = MTFSupply.getInstance(MVersion.CACHE);
 		fCache =  tfsCache.command;
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		fStd95 = null;
 		fCache = null;
 	}
 		
 	private void testCommand(TokenFactory f, String v, boolean error) {
 		try {
 			Text text = new Text(v);
 			Token t = f.tokenize(text);
 			if (error) {
 				Assert.assertTrue(t instanceof TSyntaxError);
 			} else {
 				Assert.assertFalse(t instanceof TSyntaxError);				
 			}
 			TFCommonTest.validTokenCheck(t, v);
 		} catch(SyntaxErrorException e) {
 			fail("Unexpected exception.");
 		}
 	}
 
 	private void testCommand(TokenFactory f, String v) {
 		testCommand(f, v, false);
 	}
 
 	private void testBreak(TFCommand f) {		
 		testCommand(f, "B");
 		testCommand(f, "B   ");
 		testCommand(f, "B \"+13^TAG\"");
 		testCommand(f, "B \"+13^TAG\""     );
 	}
 
 	@Test
 	public void testBreak() {
 		testBreak(fCache);
 		testBreak(fStd95);
 	}
 
 	private void testDo(TFCommand f) {
 		testCommand(f, "D SENDMSG^XMXAPI(.5,RCSUBJ,XMBODY,.XMTO,,.XMZ)");
 		testCommand(f, "D SET^IBCSC5A(BILLDA,.ARRXS,)");
 		testCommand(f, "D ^%ZIS");
 		testCommand(f, "D WRITE(IO,G)");
 		testCommand(f, "D WRAPPER(@(\"PSBTAB\"_(FLD-1))+1,((@(\"PSBTAB\"_(FLD))-(@(\"PSBTAB\"_(FLD-1))+1))),PSBVAL)");
 		testCommand(f, "D");
 		testCommand(f, "D    ");
 		testCommand(f, "D:X=Y A^B");
 		testCommand(f, "D:X=Y A+3^B,B+4^C");
 		testCommand(f, "D @A^B");
 		testCommand(f, "D A^@B");
 		testCommand(f, "D @A^@B");
 		testCommand(f, "D @ABC");
 		testCommand(f, "D @ABC,C^D,EE^FF:$$GT");
 		testCommand(f, "D:D=RR @ABC,C^D,EE^FF:$$GT");
 		testCommand(f, "D &A.B,&A.C(P0)");
 		testCommand(f, "D &B,&C(P0,.P1)");
 		testCommand(f, "D &A.B^R,&A.C^R(P0,P3)");
 		testCommand(f, "D &B^R,&C^R(,P0)");
 	}
 
 	@Test
 	public void testDo() {
 		testDo(fCache);
 		testDo(fStd95);
 		testCommand(fCache, "DO $system.Status.DecomposeStatus(%objlasterror,.XOBLERR)");
 		testCommand(fCache, "D READER.Read()");
		testCommand(fCache, "D DecomposeStatus^%SYS.DATABASE(RC,.MSGLIST,0,\"\")");
 	}
 
 	private void testFor(TFCommand f) {
 		testCommand(f, "F FLD=1:1:$L(LST,\",\")");
 		testCommand(f, "F STAT=42,16");
 	}
 
 	@Test
 	public void testFor() {
 		testFor(fCache);
 		testFor(fStd95);
 	}
 
 	private void testGoto(TFCommand f) {
 		testCommand(f, "G:POP H^XUS");
 		testCommand(f, "G:POP H^[ENV]XUS:POP");
 		testCommand(f, "G:POP H+3^[ENV]XUS:POP");
 		testCommand(f, "G H+3");
 		testCommand(f, "G ^XUS:POP");
 		testCommand(f, "G H+3,^XUS:POP");
 		testCommand(f, "G ^XUS");
 		testCommand(f, "G @A");
 		testCommand(f, "G @A^@B");
 		testCommand(f, "G @A:P");
 		testCommand(f, "G ^@B");
 		testCommand(f, "G ^@B:P");
 		testCommand(f, "G ^", true);
 		testCommand(f, "G 0^DIE17");
 	}
 
 	@Test
 	public void testGoto() {
 		testGoto(fCache);
 		testGoto(fStd95);
 	}
 
 	private void testHaltHang(TFCommand f) {
 		testCommand(f, "H 3");
 		testCommand(f, "H");
 	}
 
 	@Test
 	public void testHaltHang() {
 		testHaltHang(fCache);
 		testHaltHang(fStd95);
 	}
 
 	private void testIf(TFCommand f) {
 		testCommand(f, "I $L($T(NTRTMSG^HDISVAP))");
 		testCommand(f, "I @CLIN@(0)=0");
 		testCommand(f, "I @CLIN@(0)");
 		testCommand(f, "I $P(LA7XFORM,\"^\")?1.N,LA7VAL?1(1N.E,1\".\".E)");
 		testCommand(f, "I $D(@G)#10");
 	}
 	
 	@Test
 	public void testIf() {
 		testIf(fCache);
 		testIf(fStd95);
 	}
 
 	private void testJob(TFCommand f) {
 		testCommand(f, "JOB CHILDNT^XOBVTCPL():(:4:XOBIO:XOBIO):10");
 		testCommand(f, "J LISTENER^XOBVTCPL(XOBPORT,$GET(XOBCFG))::5");
 		testCommand(f, "JOB CHILDNT^XOBVTCPL(A,.B):(:4:XOBIO:XOBIO):10");
 		testCommand(f, "JOB CHILDNT+3^XOBVTCPL:(:4:XOBIO:XOBIO):10");
 		testCommand(f, "JOB CHILDNT+3^XOBVTCPL:5");
 		testCommand(f, "JOB CHILDNT:5");
 		testCommand(f, "JOB CHILDNT:(A:B:C):5");
 		testCommand(f, "JOB CHILDNT:(::B:C):5");
 		testCommand(f, "JOB @A^@A:(::B:C):5");
 		testCommand(f, "J ^XMRONT");
 		testCommand(f, "J ^XMRONT::5");
 	}
 	
 	@Test
 	public void testJob() {
 		testJob(fCache);
 		testJob(fStd95);
 	}
 
 	private void testKill(TFCommand f) {
 		testCommand(f, "K A");
 		testCommand(f, "K A,B,@C,D");
 		testCommand(f, "K @A");
 		testCommand(f, "K @A,@C");
 		testCommand(f, "K (A,B),D,(R,E)");
 		testCommand(f, "K A,B");
 		testCommand(f, "K CC,DD,EE");
 		testCommand(f, "K");
 		testCommand(f, "K ^XY");
 		testCommand(f, "K ^XY,^Z(\"D\")");
 		testCommand(f, "K:A=1 ^XY,^Z(\"D\")");
 		testCommand(f, "K:A=1 ^XY,D");
 		testCommand(f, "K ^XY,D,ZZ(\"A\",\"FF\"),TRE");
 		testCommand(f, "K (A)");
 		testCommand(f, "K (A,B)");
 		testCommand(f, "K %ZIS");
 		testCommand(f, "K (^A)", true);
 		testCommand(f, "K (A,^A)", true);
 		testCommand(f, "K (A(25))", true);
 		testCommand(f, "K (B,A(3,2))", true);
 		testCommand(f, "K ()", true);
 		testCommand(f, "K (,A)", true);
 		testCommand(f, "K (D,,Y)", true);
 		testCommand(f, "K CC,DD,EE,", true);
 		testCommand(f, "K CC,,EE", true);
 	}
 
 	@Test
 	public void testKill() {
 		testKill(fCache);
 		testKill(fStd95);
 	}
 
 	private void testLock(TFCommand f) {
 		testCommand(f, "L -^PRCA(430,+$G(PRCABN),0)");
 		testCommand(f, "L +^PRCA(430,DA,0):0");		
 		testCommand(f, "L -^PRCA(430,+$G(PRCABN),0),+^PRCA:0");
 		testCommand(f, "L +(^LR(LRDFN,\"MI\",LRIDT)):0");
 		testCommand(f, "L +(^LRO(68,LRAA,1,LRAD,1,LRAN))");
 		testCommand(f, "L +(^LR(LRDFN,\"MI\",LRIDT),^LRO(68,LRAA,1,LRAD,1,LRAN)):0");
 		testCommand(f, "L -(^LR(LRDFN,\"MI\",LRIDT),^LRO(68,LRAA,1,LRAD,1,LRAN))");
 		testCommand(f, "L +PSX(550.1):3");	
 	}
 
 	@Test
 	public void testLock() {
 		testLock(fCache);
 		testLock(fStd95);
 	}
 
 	public void testRead(TFCommand f) {
 		testCommand(f, "R !,\"Select DEBTOR NAME or BILL NUMBER: \",X:DTIME");
 		testCommand(f, "R !,\"ANSWER= \",@YSR1:300");
 	}
 
 	@Test
 	public void testRead() {
 		testRead(fCache);
 		testRead(fStd95);
 	}
 
 	private void testQuit(TFCommand f) {
 		testCommand(f, "Q @SCLIST@(0)>0");
 	}
 
 	@Test
 	public void testQuit() {
 		testQuit(fCache);
 		testQuit(fStd95);
 	}
 
 	private void testSet(TFCommand f) {
 		testCommand(f, "S A=B");
 		testCommand(f, "S X=$$MG^XMBGRP(\"RCCPC STATEMENTS\",0,.5,1,\"\",.DES,1)");
 		testCommand(f, "S @^%ZOSF(\"TRAP\")");
 		testCommand(f, "S X=\"ERROR^PRCAHV\",@^%ZOSF(\"TRAP\")");
 		testCommand(f, "S A=B,@C=D");
 		testCommand(f, "S @A,$E(V,\",\",2)=\"DE\"");
 		testCommand(f, "S @A=@C");
 		testCommand(f, "S $X=5,$Y=3,(B,C,D)=(A=B)");
 		testCommand(f, "S A=B,C=F,D=YSH");
 		testCommand(f, "S @A=\"S\"");
 		testCommand(f, "S @H@(0)=3");
 		testCommand(f, "S XT4=\"I 1\"   ");
 		testCommand(f, "S IOP=IOP_\";255\",%ZIS=\"\"");
 		testCommand(f, "S X=$I(^HLCS(870,DP,P),$S($G(Z):-1,1:1))");
 		testCommand(f, "S ^$W(\"ZISGTRM\",\"VISIBLE\")=1");
 	}
 
 	@Test
 	public void testSet() {
 		testSet(fCache);
 		testSet(fStd95);
 	}
 
 	private void testOpen(TFCommand f) {
 		testCommand(f, "O:$G(LOGICAL)]\"\" HLCSTATE(\"DEVICE\"):(TCPDEV:BLOCKSIZE=512):HLCSTATE(\"OPEN TIMEOUT\")");
 		testCommand(f, "OPEN XOBIO:(:XOBPORT:\"AT\"):30");
 	}	
 	
 	@Test
 	public void testOpen() {
 		testOpen(fCache);
 		testOpen(fStd95);
 	}
 
 	private void testUse(TFCommand f) {
 		testCommand(f, "U IO");
 		testCommand(f, "U A:B");
 		testCommand(f, "U $I:(64)");
 		testCommand(f, "U $I:(0::::64)");
 		testCommand(f, "U $I:(VT=1:ESCAPE=1)");
 		testCommand(f, "U $I:(:\"CT\")");
 		testCommand(f, "U $I:(ESCAPE)");
 		testCommand(f, "U 56::\"TCP\"");
 	}
 
 	@Test
 	public void testUse() {
 		testUse(fCache);
 		testUse(fStd95);
 	}
 
 	private void testView(TFCommand f) {
 		testCommand(f, "V -1:1");
 	}
 	
 	@Test
 	public void testView() {
 		testView(fCache);
 		testView(fStd95);
 	}
 
 	private void testWrite(TFCommand f) {
 		testCommand(f, "W !!,^YTT(601,YSTEST,\"G\",L,1,1,0)");
 		testCommand(f, "W !,$S($D(ZTSK):\"REQUEST QUEUED TASK=\"_ZTSK,1:\"REQUEST CANCELLED\")");
 		testCommand(f, "W:$O(^DVB(396.4,OLDA,\"RES\",LINE))]\"\"&('+$G(DVBGUI)) !!,\"Exam Results Continued\",!!");
 		testCommand(f, "W /LISTEN(1)");
 	}
 	
 	@Test
 	public void testWrite() {
 		testWrite(fCache);
 		testWrite(fStd95);
 	}
 
 	private void testXecute(TFCommand f) {
 		testCommand(f, "X ^%ZOSF(\"TYPE-AHEAD\"),^%ZOSF(\"LABOFF\")");
 	}
 	
 	@Test
 	public void testXecute() {
 		testXecute(fCache);
 		testXecute(fStd95);
 	}
 
 	private void testZ(TFCommand f) {
 		testCommand(f, "ZB ZB0^HLOQUE:\"N\":1:\"S RET=0\"");
 	}
 
 	@Test
 	public void testZ() {
 		testZ(fCache);
 		testZ(fStd95);
 	}
 }
 
