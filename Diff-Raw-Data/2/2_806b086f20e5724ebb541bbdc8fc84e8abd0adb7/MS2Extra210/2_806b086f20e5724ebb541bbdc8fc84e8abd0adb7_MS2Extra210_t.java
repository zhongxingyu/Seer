 package uk.org.smithfamily.mslogger.ecuDef;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import android.content.Context;
 
 public class MS2Extra210 extends Megasquirt
 {
 	// Flags
 	boolean						EGTFULL;
 	boolean						NARROW_BAND_EGO;
 	boolean						CELSIUS;
 	boolean						CAN_COMMANDS;
 	boolean						LAMBDA;
 	// Runtime vars
 	double						tpsDOT;
 	int							portStatus;
 	double						ignload2;
 	int							ready;
 	int							wallfuel1;
 	int							synccnt;
 	int							wallfuel2;
 	double						knock;
 	double						afr2;
 	double						afr1;
 	int							EAEFuelCorr2;
 	int							EAEFuelCorr1;
 	int							rpm;
 	int							boostduty;
 	double						coolant;
 	double						afrtgt2;
 	int							tpsADC;
 	int							engine;
 	int							tpsaccaen;
 	double						afrtgt1;
 	double						looptime;
 	int							tpsaccden;
 	int							crank;
 	int							status1;
 	int							inj2;
 	int							inj1;
 	int							port0;
 	int							port4;
 	int							port3;
 	int							port2;
 	int							port1;
 	int							gpioadc0;
 	int							maf;
 	int							port6;
 	int							port5;
 	double						map;
 	double						egoV;
 	int							warmup;
 	int							firing1;
 	double						mat;
 	int							firing2;
 	double						timing_err;
 	int							gpioadc1;
 	int							gpioadc2;
 	int							gpioadc3;
 	int							gpioadc4;
 	int							gpioadc5;
 	int							gpioadc6;
 	int							gpioadc7;
 	double						egoV2;
 	int							user0;
 	int							seconds;
 	int							startw;
 	int							warmupEnrich;
 	double						dwell;
 	int							gammaEnrich;
 	double						egoCorrection1;
 	double						egoCorrection2;
 	int							mapaccden;
 	double						fuelload;
 	int							mapDOT;
 	double						barometer;
 	int							wbo2_en2;
 	double						coldAdvDeg;
 	int							gpioport2;
 	int							gpioport1;
 	int							gpioport0;
 	int							wbo2_en1;
 	int							status5;
 	int							status4;
 	int							status3;
 	int							status2;
 	int							baroCorrection;
 	int							tpsfuelcut;
 	double						pulseWidth2;
 	double						pulseWidth1;
 	double						veCurr1;
 	double						veCurr2;
 	double						batteryVoltage;
 	double						fuelload2;
 	int							gpiopwmin0;
 	int							syncreason;
 	double						ignload;
 	int							gpiopwmin1;
 	int							gpiopwmin2;
 	int							gpiopwmin3;
 	int							iacstep;
 	double						accelEnrich;
 	double						knockRetard;
 	int							adc7;
 	int							adc6;
 	int							airCorrection;
 	int							squirt;
 	int							deltaT;
 	int							sched2;
 	double						idleDC;
 	int							sched1;
 	int							fuelCorrection;
 	double						advance;
 	double						tps;
 	int							mapaccaen;
 
 	// eval vars
 	int							accDecEnrich;
 	double						egt6temp;
 	double						throttle;
 	double						dutyCycle2;
 	int							dbg_cam_filt;
 	int							deadValue;
 	double						dutyCycle1;
 	double						pulseWidth;
 	int							dbg_crk_mask;
 	double						cycleTime1;
 	double						egt7temp;
 	double						cycleTime2;
 	int							altDiv1;
 	double						time;
 	int							altDiv2;
 	double						veCurr;
 	double						egoCorrection;
 	double						rpm100;
 	int							nSquirts2;
 	int							dbg_cam_mask;
 	int							nSquirts1;
 	double						lambda2;
 	double						lambda1;
 	int							secl;
 	double						egoVoltage;
 	int							dbg_crk_filt;
 
 	private static final String	MS2_EXTRA_REL_2_1_0Q	= "MS2Extra Rel 2.1.0q\0";
 	private static final String	MS2_EXTRA_REL_2_1_0P	= "MS2Extra Rel 2.1.0p\0";
 	private byte[]				ochCommand				= { 65 };
 	private byte[]				sigCommand				= { 81 };
 
 	// Constants
 	int							alternate;
 	int							twoStroke;
 	int							nCylinders;
 	int							divider;
 
 	private Set<String>			sigs					= new HashSet<String>(Arrays.asList(new String[] { MS2_EXTRA_REL_2_1_0P,
 			MS2_EXTRA_REL_2_1_0Q						}));
 
 	@Override
 	public Set<String> getSignature()
 	{
 		return sigs;
 	}
 
 	public MS2Extra210(Context c)
 	{
 		super(c);
 		NARROW_BAND_EGO = isSet("NARROW_BAND_EGO");
 		LAMBDA = isSet("LAMBDA");
 		EGTFULL = isSet("EGTFULL");
 		CELSIUS = isSet("CELSIUS");
 	}
 
 	@Override
 	public byte[] getOchCommand()
 	{
 		return ochCommand;
 	}
 
 	@Override
 	public byte[] getSigCommand()
 	{
 		return sigCommand;
 	}
 
 	@Override
 	public void loadConstants(boolean simulated) throws IOException
 	{
 
 		if (simulated)
 		{
 			alternate = 1;
 			twoStroke = 0;
 			nCylinders = 8;
 			divider = 1;
 		}
 		else
 		{
 			byte[] pageBuffer = new byte[1024];
 
 			byte[] selectPage1 = { 114, 0, 4, 0, 0, 4, 0 };
 
 			getPage(pageBuffer, selectPage1, null);
 			alternate = MSUtils.getBits(pageBuffer, 611, 0, 0);
 			twoStroke = MSUtils.getBits(pageBuffer, 617, 0, 0);
 			nCylinders = MSUtils.getBits(pageBuffer, 0, 0, 3);
 			divider = MSUtils.getByte(pageBuffer, 610);
 		}
 	}
 
 	@Override
 	public int getBlockSize()
 	{
 		return 145;
 	}
 
 	@Override
 	public int getPageActivationDelay()
 	{
 		return 50;
 	}
 
 	@Override
 	public int getInterWriteDelay()
 	{
 		return 5;
 	}
 
 	@Override
 	public int getCurrentTPS()
 	{
		return tpsADC;
 	}
 
 	@Override
 	public int getSigSize()
 	{
 		return MS2_EXTRA_REL_2_1_0Q.length();
 	}
 
 	@Override
 	public void mapDispValues()
 	{
 		dispRPM = rpm;
 		dispMAP = map;
 		if (NARROW_BAND_EGO)
 		{
 			dispAFR = egoVoltage;
 		}
 		else if (LAMBDA)
 		{
 			dispAFR = lambda1;
 		}
 		else
 		{
 			dispAFR = afr1;
 		}
 		dispCLT = coolant;
 		dispIAT = mat;
 		dispADV = advance;
 	}
 
 	@Override
 	public void calculate(byte[] ochBuffer) throws IOException
 	{
 		deadValue = (0);
 		if (CAN_COMMANDS)
 		{
 
 		}
 		else
 		{
 
 		}
 
 		seconds = (int) ((MSUtils.getWord(ochBuffer, 0) + 0.0) * 1.000);
 		secl = (seconds % 256);
 		pulseWidth1 = (double) ((MSUtils.getWord(ochBuffer, 2) + 0.0) * 0.000666);
 		pulseWidth2 = (double) ((MSUtils.getWord(ochBuffer, 4) + 0.0) * 0.000666);
 		pulseWidth = (pulseWidth1);
 		rpm = (int) ((MSUtils.getWord(ochBuffer, 6) + 0.0) * 1.000);
 		advance = (double) ((MSUtils.getSignedWord(ochBuffer, 8) + 0.0) * 0.100);
 		squirt = (int) ((MSUtils.getByte(ochBuffer, 10) + 0.0) * 1.000);
 		firing1 = MSUtils.getBits(ochBuffer, 10, 0, 0);
 		firing2 = MSUtils.getBits(ochBuffer, 10, 1, 1);
 		sched1 = MSUtils.getBits(ochBuffer, 10, 2, 2);
 		inj1 = MSUtils.getBits(ochBuffer, 10, 3, 3);
 		sched2 = MSUtils.getBits(ochBuffer, 10, 4, 4);
 		inj2 = MSUtils.getBits(ochBuffer, 10, 5, 5);
 		engine = (int) ((MSUtils.getByte(ochBuffer, 11) + 0.0) * 1.000);
 		ready = MSUtils.getBits(ochBuffer, 11, 0, 0);
 		crank = MSUtils.getBits(ochBuffer, 11, 1, 1);
 		startw = MSUtils.getBits(ochBuffer, 11, 2, 2);
 		warmup = MSUtils.getBits(ochBuffer, 11, 3, 3);
 		tpsaccaen = MSUtils.getBits(ochBuffer, 11, 4, 4);
 		tpsaccden = MSUtils.getBits(ochBuffer, 11, 5, 5);
 		mapaccaen = MSUtils.getBits(ochBuffer, 11, 6, 6);
 		mapaccden = MSUtils.getBits(ochBuffer, 11, 7, 7);
 		afrtgt1 = (double) ((MSUtils.getByte(ochBuffer, 12) + 0.0) * 10.00);
 		afrtgt2 = (double) ((MSUtils.getByte(ochBuffer, 13) + 0.0) * 10.00);
 		wbo2_en1 = (int) ((MSUtils.getByte(ochBuffer, 14) + 0.0) * 1.000);
 		wbo2_en2 = (int) ((MSUtils.getByte(ochBuffer, 15) + 0.0) * 1.000);
 		barometer = (double) ((MSUtils.getSignedWord(ochBuffer, 16) + 0.0) * 0.100);
 		map = (double) ((MSUtils.getSignedWord(ochBuffer, 18) + 0.0) * 0.100);
 		if (CELSIUS)
 		{
 			mat = (double) ((MSUtils.getSignedWord(ochBuffer, 20) + -320.0) * 0.05555);
 			coolant = (double) ((MSUtils.getSignedWord(ochBuffer, 22) + -320.0) * 0.05555);
 
 		}
 		else
 		{
 			mat = (double) ((MSUtils.getSignedWord(ochBuffer, 20) + 0.0) * 0.100);
 			coolant = (double) ((MSUtils.getSignedWord(ochBuffer, 22) + 0.0) * 0.100);
 
 		}
 
 		tps = (double) ((MSUtils.getSignedWord(ochBuffer, 24) + 0.0) * 0.100);
 		throttle = (tps);
 		batteryVoltage = (double) ((MSUtils.getSignedWord(ochBuffer, 26) + 0.0) * 0.100);
 		afr1 = (double) ((MSUtils.getSignedWord(ochBuffer, 28) + 0.0) * 0.100);
 		afr2 = (double) ((MSUtils.getSignedWord(ochBuffer, 30) + 0.0) * 0.100);
 		lambda1 = (afr1 / 14.7);
 		lambda2 = (afr2 / 14.7);
 		knock = (double) ((MSUtils.getSignedWord(ochBuffer, 32) + 0.0) * 0.100);
 		egoCorrection1 = (double) ((MSUtils.getSignedWord(ochBuffer, 34) + 0.0) * 0.1000);
 		egoCorrection = ((egoCorrection1 + egoCorrection2) / 2);
 		egoCorrection2 = (double) ((MSUtils.getSignedWord(ochBuffer, 36) + 0.0) * 0.1000);
 		airCorrection = (int) ((MSUtils.getSignedWord(ochBuffer, 38) + 0.0) * 1.000);
 		warmupEnrich = (int) ((MSUtils.getSignedWord(ochBuffer, 40) + 0.0) * 1.000);
 		accelEnrich = (double) ((MSUtils.getSignedWord(ochBuffer, 42) + 0.0) * 0.100);
 		tpsfuelcut = (int) ((MSUtils.getSignedWord(ochBuffer, 44) + 0.0) * 1.000);
 		baroCorrection = (int) ((MSUtils.getSignedWord(ochBuffer, 46) + 0.0) * 1.000);
 		gammaEnrich = (int) ((MSUtils.getSignedWord(ochBuffer, 48) + 0.0) * 1.000);
 		veCurr1 = (double) ((MSUtils.getSignedWord(ochBuffer, 50) + 0.0) * 0.1000);
 		veCurr2 = (double) ((MSUtils.getSignedWord(ochBuffer, 52) + 0.0) * 0.1000);
 		veCurr = (veCurr1);
 		iacstep = (int) ((MSUtils.getSignedWord(ochBuffer, 54) + 0.0) * 1.000);
 		idleDC = (double) ((MSUtils.getSignedWord(ochBuffer, 54) + 0.0) * 0.39063);
 		coldAdvDeg = (double) ((MSUtils.getSignedWord(ochBuffer, 56) + 0.0) * 0.100);
 		tpsDOT = (double) ((MSUtils.getSignedWord(ochBuffer, 58) + 0.0) * 0.100);
 		mapDOT = (int) ((MSUtils.getSignedWord(ochBuffer, 60) + 0.0) * 1.000);
 		dwell = (double) ((MSUtils.getWord(ochBuffer, 62) + 0.0) * 0.0666);
 		maf = (int) ((MSUtils.getSignedWord(ochBuffer, 64) + 0.0) * 1.000);
 		fuelload = (double) ((MSUtils.getSignedWord(ochBuffer, 66) + 0.0) * 0.100);
 		fuelCorrection = (int) ((MSUtils.getSignedWord(ochBuffer, 68) + 0.0) * 1.000);
 		portStatus = (int) ((MSUtils.getByte(ochBuffer, 70) + 0.0) * 1.000);
 		port0 = MSUtils.getBits(ochBuffer, 70, 0, 0);
 		port1 = MSUtils.getBits(ochBuffer, 70, 1, 1);
 		port2 = MSUtils.getBits(ochBuffer, 70, 2, 2);
 		port3 = MSUtils.getBits(ochBuffer, 70, 3, 3);
 		port4 = MSUtils.getBits(ochBuffer, 70, 4, 4);
 		port5 = MSUtils.getBits(ochBuffer, 70, 5, 5);
 		port6 = MSUtils.getBits(ochBuffer, 70, 6, 6);
 		knockRetard = (double) ((MSUtils.getByte(ochBuffer, 71) + 0.0) * 0.1);
 		EAEFuelCorr1 = (int) ((MSUtils.getWord(ochBuffer, 72) + 0.0) * 1.0);
 		egoV = (double) ((MSUtils.getSignedWord(ochBuffer, 74) + 0.0) * 0.01);
 		egoV2 = (double) ((MSUtils.getSignedWord(ochBuffer, 76) + 0.0) * 0.01);
 		status1 = (int) ((MSUtils.getByte(ochBuffer, 78) + 0.0) * 1.0);
 		status2 = (int) ((MSUtils.getByte(ochBuffer, 79) + 0.0) * 1.0);
 		status3 = (int) ((MSUtils.getByte(ochBuffer, 80) + 0.0) * 1.0);
 		status4 = (int) ((MSUtils.getByte(ochBuffer, 81) + 0.0) * 1.0);
 		looptime = (double) ((MSUtils.getWord(ochBuffer, 82) + 0.0) * 0.6667);
 		status5 = (int) ((MSUtils.getWord(ochBuffer, 84) + 0) * 1);
 		tpsADC = (int) ((MSUtils.getWord(ochBuffer, 86) + 0) * 1);
 		fuelload2 = (double) ((MSUtils.getWord(ochBuffer, 88) + 0.0) * 0.100);
 		ignload = (double) ((MSUtils.getWord(ochBuffer, 90) + 0.0) * 0.100);
 		ignload2 = (double) ((MSUtils.getWord(ochBuffer, 92) + 0.0) * 0.100);
 		synccnt = (int) ((MSUtils.getByte(ochBuffer, 94) + 0) * 1);
 		timing_err = (double) ((MSUtils.getSignedByte(ochBuffer, 95) + 0) * 0.1);
 		deltaT = (int) ((MSUtils.getSignedLong(ochBuffer, 96) + 0.0) * 1.000);
 		wallfuel1 = (int) ((MSUtils.getLong(ochBuffer, 100) + 0.0) * 1.000);
 		gpioadc0 = (int) ((MSUtils.getWord(ochBuffer, 104) + 0.0) * 1.000);
 		gpioadc1 = (int) ((MSUtils.getWord(ochBuffer, 106) + 0.0) * 1.000);
 		gpioadc2 = (int) ((MSUtils.getWord(ochBuffer, 108) + 0.0) * 1.000);
 		gpioadc3 = (int) ((MSUtils.getWord(ochBuffer, 110) + 0.0) * 1.000);
 		gpioadc4 = (int) ((MSUtils.getWord(ochBuffer, 112) + 0.0) * 1.000);
 		gpioadc5 = (int) ((MSUtils.getWord(ochBuffer, 114) + 0.0) * 1.000);
 		gpioadc6 = (int) ((MSUtils.getWord(ochBuffer, 116) + 0.0) * 1.000);
 		gpioadc7 = (int) ((MSUtils.getWord(ochBuffer, 118) + 0.0) * 1.000);
 		gpiopwmin0 = (int) ((MSUtils.getWord(ochBuffer, 120) + 0.0) * 1.000);
 		gpiopwmin1 = (int) ((MSUtils.getWord(ochBuffer, 122) + 0.0) * 1.000);
 		gpiopwmin2 = (int) ((MSUtils.getWord(ochBuffer, 124) + 0.0) * 1.000);
 		gpiopwmin3 = (int) ((MSUtils.getWord(ochBuffer, 126) + 0.0) * 1.000);
 		adc6 = (int) ((MSUtils.getWord(ochBuffer, 128) + 0.0) * 1);
 		adc7 = (int) ((MSUtils.getWord(ochBuffer, 130) + 0.0) * 1);
 		wallfuel2 = (int) ((MSUtils.getLong(ochBuffer, 132) + 0.0) * 1.000);
 		EAEFuelCorr2 = (int) ((MSUtils.getWord(ochBuffer, 136) + 0.0) * 1.0);
 		boostduty = (int) ((MSUtils.getByte(ochBuffer, 138) + 0.0) * 1.0);
 		syncreason = (int) ((MSUtils.getByte(ochBuffer, 139) + 0.0) * 1.0);
 		user0 = (int) ((MSUtils.getWord(ochBuffer, 140) + 0.0) * 1.0);
 		gpioport0 = (int) ((MSUtils.getByte(ochBuffer, 142) + 0.0) * 1.000);
 		gpioport1 = (int) ((MSUtils.getByte(ochBuffer, 143) + 0.0) * 1.000);
 		gpioport2 = (int) ((MSUtils.getByte(ochBuffer, 144) + 0.0) * 1.000);
 		accDecEnrich = (((accelEnrich + (tpsaccden) != 0) ? tpsfuelcut : 100));
 		time = (timeNow());
 		rpm100 = (rpm / 100.0);
 		altDiv1 = (((alternate) != 0) ? 2 : 1);
 		altDiv2 = (((alternate) != 0) ? 2 : 1);
 		cycleTime1 = (60000.0 / rpm * (2.0 - twoStroke));
 		nSquirts1 = (nCylinders / divider);
 		dutyCycle1 = (100.0 * nSquirts1 / altDiv1 * pulseWidth1 / cycleTime1);
 		cycleTime2 = (60000.0 / rpm * (2.0 - twoStroke));
 		nSquirts2 = (nCylinders / divider);
 		dutyCycle2 = (100.0 * nSquirts2 / altDiv2 * pulseWidth2 / cycleTime2);
 		if (NARROW_BAND_EGO)
 		{
 			egoVoltage = (1.0 - (afr1 * 0.04883));
 
 		}
 		else if (LAMBDA)
 		{
 			egoVoltage = (lambda1);
 
 		}
 		else
 		{
 			egoVoltage = (afr1);
 
 		}
 
 		if (EGTFULL)
 		{
 			if (CELSIUS)
 			{
 				egt6temp = (adc6 * 1.222);
 				egt7temp = (adc7 * 1.222);
 
 			}
 			else
 			{
 				egt6temp = ((adc6 * 2.200) + 32);
 				egt7temp = ((adc7 * 2.200) + 32);
 
 			}
 
 		}
 		else
 		{
 			if (CELSIUS)
 			{
 				egt6temp = (adc6 * 0.956);
 				egt7temp = (adc7 * 0.956);
 
 			}
 			else
 			{
 				egt6temp = ((adc6 * 1.721) + 32);
 				egt7temp = ((adc7 * 1.721) + 32);
 
 			}
 
 		}
 
 		dbg_crk_mask = (gpioadc6 & 0xff);
 		dbg_crk_filt = (gpioadc6 >> 8);
 		dbg_cam_mask = (gpioadc7 & 0xff);
 		dbg_cam_filt = (gpioadc7 >> 8);
 	}
 
 	@Override
 	public String getLogHeader()
 	{
 		StringBuffer b = new StringBuffer();
 		b.append("Time").append("\t");
 		b.append("SecL").append("\t");
 		b.append("RPM").append("\t");
 		b.append("MAP").append("\t");
 		b.append("TP").append("\t");
 		if (NARROW_BAND_EGO)
 		{
 			b.append("O2").append("\t");
 
 		}
 		else if (LAMBDA)
 		{
 			b.append("Lambda").append("\t");
 
 		}
 		else
 		{
 			b.append("AFR").append("\t");
 
 		}
 
 		b.append("MAT").append("\t");
 		b.append("CLT").append("\t");
 		b.append("Engine").append("\t");
 		b.append("Gego").append("\t");
 		b.append("Gair").append("\t");
 		b.append("Gwarm").append("\t");
 		b.append("Gbaro").append("\t");
 		b.append("Gammae").append("\t");
 		b.append("TPSacc").append("\t");
 		b.append("Gve").append("\t");
 		b.append("PW").append("\t");
 		b.append("DutyCycle1").append("\t");
 		b.append("Gve2").append("\t");
 		b.append("PW2").append("\t");
 		b.append("DutyCycle2").append("\t");
 		b.append("SparkAdv").append("\t");
 		b.append("knockRet").append("\t");
 		b.append("ColdAdv").append("\t");
 		b.append("Dwell").append("\t");
 		b.append("tpsDOT").append("\t");
 		b.append("mapDOT").append("\t");
 		b.append("IACstep").append("\t");
 		b.append("Batt V").append("\t");
 		b.append("deltaT").append("\t");
 		b.append("WallFuel1").append("\t");
 		b.append("WallFuel2").append("\t");
 		b.append("EAE1 %").append("\t");
 		b.append("EAE2 %").append("\t");
 		b.append("Load").append("\t");
 		b.append("Secondary Load").append("\t");
 		b.append("Ign load").append("\t");
 		b.append("Secondary Ign Load").append("\t");
 		b.append("EGT 6 temp").append("\t");
 		b.append("EGT 7 temp").append("\t");
 		b.append("gpioadc0").append("\t");
 		b.append("gpioadc1").append("\t");
 		b.append("gpioadc2").append("\t");
 		b.append("gpioadc3").append("\t");
 		b.append("status1").append("\t");
 		b.append("status2").append("\t");
 		b.append("status3").append("\t");
 		b.append("status4").append("\t");
 		b.append("status5").append("\t");
 		b.append("timing err%").append("\t");
 		b.append("AFR Target 1").append("\t");
 		b.append("Boost Duty").append("\t");
 		b.append("PWM Idle Duty").append("\t");
 		b.append("Lost sync count").append("\t");
 		b.append("Lost sync reason").append("\t");
 		return b.toString();
 	}
 
 	@Override
 	public String getLogRow()
 	{
 		StringBuffer b = new StringBuffer();
 		b.append(time).append("\t");
 		b.append(seconds).append("\t");
 		b.append(rpm).append("\t");
 		b.append(round(map)).append("\t");
 		b.append(throttle).append("\t");
 		if (NARROW_BAND_EGO)
 		{
 			b.append(egoVoltage).append("\t");
 		}
 		else if (LAMBDA)
 		{
 			b.append(lambda1).append("\t");
 		}
 		else
 		{
 			b.append(round(afr1)).append("\t");
 		}
 		b.append(round(mat)).append("\t");
 		b.append(round(coolant)).append("\t");
 		b.append(engine).append("\t");
 		b.append(egoCorrection).append("\t");
 		b.append(airCorrection).append("\t");
 		b.append(warmupEnrich).append("\t");
 		b.append(baroCorrection).append("\t");
 		b.append(gammaEnrich).append("\t");
 		b.append(accDecEnrich).append("\t");
 		b.append(round(veCurr1)).append("\t");
 		b.append(round(pulseWidth1)).append("\t");
 		b.append(dutyCycle1).append("\t");
 		b.append(round(veCurr2)).append("\t");
 		b.append(round(pulseWidth2)).append("\t");
 		b.append(dutyCycle2).append("\t");
 		b.append(round(advance)).append("\t");
 		b.append(round(knockRetard)).append("\t");
 		b.append(round(coldAdvDeg)).append("\t");
 		b.append(round(dwell)).append("\t");
 		b.append(round(tpsDOT)).append("\t");
 		b.append(mapDOT).append("\t");
 		b.append(iacstep).append("\t");
 		b.append(round(batteryVoltage)).append("\t");
 		b.append(deltaT).append("\t");
 		b.append(wallfuel1).append("\t");
 		b.append(wallfuel2).append("\t");
 		b.append(EAEFuelCorr1).append("\t");
 		b.append(EAEFuelCorr2).append("\t");
 		b.append(round(fuelload)).append("\t");
 		b.append(round(fuelload2)).append("\t");
 		b.append(round(ignload)).append("\t");
 		b.append(round(ignload2)).append("\t");
 		b.append(egt6temp).append("\t");
 		b.append(egt7temp).append("\t");
 		b.append(gpioadc0).append("\t");
 		b.append(gpioadc1).append("\t");
 		b.append(gpioadc2).append("\t");
 		b.append(gpioadc3).append("\t");
 		b.append(status1).append("\t");
 		b.append(status2).append("\t");
 		b.append(status3).append("\t");
 		b.append(status4).append("\t");
 		b.append(status5).append("\t");
 		b.append(round(timing_err)).append("\t");
 		b.append(round(afrtgt1)).append("\t");
 		b.append(boostduty).append("\t");
 		b.append(round(idleDC)).append("\t");
 		b.append(synccnt).append("\t");
 		b.append(syncreason).append("\t");
 		return b.toString();
 	}
 }
