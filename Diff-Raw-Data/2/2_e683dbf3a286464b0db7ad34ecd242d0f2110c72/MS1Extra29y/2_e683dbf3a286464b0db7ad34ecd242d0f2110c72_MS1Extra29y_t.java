 package uk.org.smithfamily.mslogger.ecuDef;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import uk.org.smithfamily.mslogger.widgets.GaugeDetails;
 import uk.org.smithfamily.mslogger.widgets.GaugeRegister;
 
 import android.content.Context;
 
 public class MS1Extra29y extends Megasquirt
 {
 
 	private static final String	MS1_EXTRA_FORMAT_029Y3	= "MS1/Extra format 029y3 *********";
 	// Page constants START
 	int							alternate1;
 	int							alternate2;
 	int							twoStroke1;
 	int							nCylinders1;
 	int							divider1;
 	int							twoStroke2;
 	int							nCylinders2;
 	int							divider2;
 	int							injOpen1;
 	// Page constants END
 
 	byte[]						sigCommand				= { 83 };															// 'S'
 	byte[]						ochCommand				= { 82 };															// 'R'
 	private Set<String>			sigs					= new HashSet<String>(
 																Arrays.asList(new String[] { MS1_EXTRA_FORMAT_029Y3 }));
 
 	@Override
 	public Set<String> getSignature()
 	{
 		return sigs;
 	}
 
 	public MS1Extra29y(Context c)
 	{
 		super(c);
 		CELSIUS = isSet("CELSIUS");
 		NARROW_BAND_EGO = isSet("NARROW_BAND_EGO");
 		ZEITRONIX_NON_LINEAR = isSet("ZEITRONIX_NON_LINEAR");
 		INNOVATE_LC1_DEFAULT = isSet("INNOVATE_LC1_DEFAULT");
 		MPXH6300A = isSet("MPXH6300A");
 		MPXH6400A = isSet("MPXH6400A");
 		MPX4250 = isSet("MPX4250");
 	}
 
 	@Override
 	public int getInterWriteDelay()
 	{
 		return 10;
 	}
 
 	@Override
 	public int getPageActivationDelay()
 	{
 		return 100;
 	}
 
 	@Override
 	public byte[] getOchCommand()
 	{
 		return ochCommand;
 	}
 
 	@Override
 	public int getBlockSize()
 	{
 		return 39;
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
 			alternate1 = 1;
 			twoStroke1 = 0;
 			nCylinders1 = 8;
 			injOpen1 = 5;
 			divider1 = 1;
 			alternate2 = 1;
 			twoStroke2 = 0;
 			nCylinders2 = 8;
 			divider2 = 1;
 		}
 		else
 		{
 			byte[] pageBuffer1 = new byte[189];
 			byte[] pageBuffer2 = new byte[189];
 
 			byte[] selectPage1 = { 80, 1 };
 			byte[] selectPage2 = { 80, 2 };
 			byte[] readPage = { 86 };
 			getPage(pageBuffer1, selectPage1, readPage);
 			getPage(pageBuffer2, selectPage2, readPage);
 
 			alternate1 = MSUtils.getBits(pageBuffer1, 150, 0, 0);
 			injOpen1 = MSUtils.getByte(pageBuffer1, 151);
 			twoStroke1 = MSUtils.getBits(pageBuffer1, 182, 2, 2);
 			nCylinders1 = MSUtils.getBits(pageBuffer1, 182, 4, 7) + 1;
 			divider1 = pageBuffer1[149];
 
 			alternate2 = MSUtils.getBits(pageBuffer2, 150, 0, 0);
 			twoStroke2 = MSUtils.getBits(pageBuffer2, 182, 2, 2);
 			nCylinders2 = MSUtils.getBits(pageBuffer2, 182, 4, 7) + 1;
 			divider2 = pageBuffer2[149];
 		}
 	}
 
 	@Override
 	public int getCurrentTPS()
 	{
 		return tpsADC;
 	}
 
 	@Override
 	public int getSigSize()
 	{
 		return MS1_EXTRA_FORMAT_029Y3.length();
 	}
 
 	@Override
 	public void mapDispValues()
 	{
 		dispRPM = (int) RpmHiRes;
 		dispMAP = (int) map;
 		dispAFR = afr2;
 		dispCLT = coolant;
 		dispIAT = mat;
 		dispADV = advSpark;
 	}
 
 	private String[]	defaultGauges	= { "mapGauge", "tachometer", "afrGauge", "cltGauge", "matGauge" };
 
 	@Override
 	public String[] defaultGauges()
 	{
 		return defaultGauges;
 	}
 
 	// Flags
 	boolean	NARROW_BAND_EGO;
 	boolean	FAST_WIDEBAND_O2;
 	boolean	CELSIUS;
 	boolean	MSLVV_COMPATIBLE;
 	boolean	MPXH6300A;
 	boolean	MPX4250;
 	boolean	INNOVATE_LC1_DEFAULT;
 	boolean	ZEITRONIX_NON_LINEAR;
 	boolean	MPXH6400A;
 	// Runtime vars
 	int		egoADC;
 	int		fuelADC;
 	int		afrtarget;
 	int		cltADC;
 	int		matADC;
 	int		batADC;
 	int		stackL;
 	double	pulseWidth2;
 	double	pulseWidth1;
 	int		gammaEnrich;
 	int		KnockAngle;
 	int		egoCorrection2;
 	int		egtADC;
 	int		veCurr1;
 	int		veCurr2;
 	int		tpsLast;
 	int		CltIatAngle;
 	double	bcDC;
 	int		iTime;
 	int		egoCorrection;
 	int		rpm100;
 	int		portb;
 	int		porta;
 	int		portd;
 	int		portc;
 	int		tpsADC;
 	int		iTimeX;
 	int		engine;
 	int		baroADC;
 	int		secl;
 	int		accelEnrich;
 	int		airCorrection;
 	int		squirt;
 	int		mapADC;
 	int		idleDC;
 	int		warmupEnrich;
 	int		baroCorrection;
 	int		advance;
 
 	// eval vars
 	int		tpsDOT;
 	double	accDecEnrich;
 	double	mph;
 	double	mpg;
 	double	XForce;
 	int		YOffset;
 	double	throttle;
 	double	dutyCycle2;
 	int		test;
 	double	dutyCycle1;
 	int		XOffset;
 	double	Open_Time2;
 	int		altDiv1;
 	double	RpmHitmp;
 	double	time;
 	double	afr2;
 	int		altDiv2;
 	double	Open_Time1;
 	double	Cd;
 	int		rpm;
 	int		Timeroll;
 	int		mapDOT;
 	double	barometer;
 	double	dutyCy2Real;
 	double	YForce;
 	double	advSpark;
 	double	coolant;
 	double	dutyCy1Real;
 	int		nSquirts2;
 	int		nSquirts1;
 	int		RpmHiRes;
 	double	CCpHr;
 	int		nosActive1;
 	int		test2;
 	double	gph;
 	double	USgph;
 	double	USmpg;
 	int		Mass;
 	double	squirts;
 	double	fuelvolt;
 	double	mphTemp;
 	double	boost;
 	double	mapDOTTY;
 	double	OpenWidth;
 	double	ego2Voltage;
 	int		deadValue;
 	double	DiffRa;
 	double	pulseWidth;
 	int		fuelCC;
 	double	batteryVoltage;
 	double	GrTms;
 	double	boostVac;
 	double	cycleTime1;
 	double	cycleTime2;
 	double	tpsDOTTY;
 	int		veCurr;
 	int		CltIatAng;
 	int		KnockDeg;
 	int		waterIlog;
 	int		floodclear;
 	int		KnockAng;
 	double	map;
 	int		Crr;
 	double	fuelpress;
 	double	vacuum;
 	int		CltIatDeg;
 	double	lambda2;
 	double	mat;
 	int		Speed;
 	int		iTimefull;
 	double	Radius;
 	double	egoVoltage;
 	double	MAFVolts;
 	double	egttemp;
 	double	squirtmul;
 	int		InjectorRating1;
 	int		InjectorRating2;
 
 	@Override
 	public void calculate(byte[] ochBuffer) throws IOException
 	{
 		deadValue = (0);
 		secl = (int) ((MSUtils.getByte(ochBuffer, 0) + 0.000) * 1.000);
 		squirt = (int) ((MSUtils.getByte(ochBuffer, 1) + 0.000) * 1.000);
 		engine = (int) ((MSUtils.getByte(ochBuffer, 2) + 0.000) * 1.000);
 		baroADC = (int) ((MSUtils.getByte(ochBuffer, 3) + 0.000) * 1.000);
 		mapADC = (int) ((MSUtils.getByte(ochBuffer, 4) + 0.000) * 1.000);
 		matADC = (int) ((MSUtils.getByte(ochBuffer, 5) + 0.000) * 1.000);
 		cltADC = (int) ((MSUtils.getByte(ochBuffer, 6) + 0.000) * 1.000);
 		tpsADC = (int) ((MSUtils.getByte(ochBuffer, 7) + 0.000) * 1.000);
 		batADC = (int) ((MSUtils.getByte(ochBuffer, 8) + 0.000) * 1.000);
 		egoADC = (int) ((MSUtils.getByte(ochBuffer, 9) + 0.000) * 1.000);
 		egoCorrection = (int) ((MSUtils.getByte(ochBuffer, 10) + 0.000) * 1.000);
 		airCorrection = (int) ((MSUtils.getByte(ochBuffer, 11) + 0.000) * 1.000);
 		warmupEnrich = (int) ((MSUtils.getByte(ochBuffer, 12) + 0.000) * 1.000);
 		rpm100 = (int) ((MSUtils.getByte(ochBuffer, 13) + 0.000) * 1.000);
 		pulseWidth1 = (double) ((MSUtils.getByte(ochBuffer, 14) + 0.000) * 0.100);
 		accelEnrich = (int) ((MSUtils.getByte(ochBuffer, 15) + 0.000) * 1.000);
 		baroCorrection = (int) ((MSUtils.getByte(ochBuffer, 16) + 0.000) * 1.000);
 		gammaEnrich = (int) ((MSUtils.getByte(ochBuffer, 17) + 0.000) * 1.000);
 		veCurr1 = (int) ((MSUtils.getByte(ochBuffer, 18) + 0.000) * 1.000);
 		pulseWidth2 = (double) ((MSUtils.getByte(ochBuffer, 19) + 0.000) * 0.100);
 		veCurr2 = (int) ((MSUtils.getByte(ochBuffer, 20) + 0.000) * 1.000);
 		idleDC = (int) ((MSUtils.getByte(ochBuffer, 21) + 0.000) * 1.000);
 		iTime = (int) ((MSUtils.getWord(ochBuffer, 22) + 0.000) * 1.000);
 		advance = (int) ((MSUtils.getByte(ochBuffer, 24) + 0.000) * 1.000);
 		afrtarget = (int) ((MSUtils.getByte(ochBuffer, 25) + 0.000) * 1.000);
 		fuelADC = (int) ((MSUtils.getByte(ochBuffer, 26) + 0.000) * 1.000);
 		egtADC = (int) ((MSUtils.getByte(ochBuffer, 27) + 0.000) * 1.000);
 		CltIatAngle = (int) ((MSUtils.getByte(ochBuffer, 28) + 0.000) * 1.000);
 		KnockAngle = (int) ((MSUtils.getByte(ochBuffer, 29) + 0.000) * 1.000);
 		egoCorrection2 = (int) ((MSUtils.getByte(ochBuffer, 30) + 0.000) * 1.000);
 		porta = (int) ((MSUtils.getByte(ochBuffer, 31) + 0) * 1.000);
 		portb = (int) ((MSUtils.getByte(ochBuffer, 32) + 0) * 1.000);
 		portc = (int) ((MSUtils.getByte(ochBuffer, 33) + 0) * 1.000);
 		portd = (int) ((MSUtils.getByte(ochBuffer, 34) + 0) * 1.000);
 		stackL = (int) ((MSUtils.getByte(ochBuffer, 35) + 0) * 1.000);
 		tpsLast = (int) ((MSUtils.getByte(ochBuffer, 36) + 0) * 1.000);
 		iTimeX = (int) ((MSUtils.getByte(ochBuffer, 37) + 0.000) * 1.000);
 		bcDC = (double) ((MSUtils.getByte(ochBuffer, 38) + 0.000) * 0.3922);
 		accDecEnrich = ((((engine & 32)) != 0) ? 100
 				: ((pulseWidth - injOpen1) / (pulseWidth - (accelEnrich / 10) - injOpen1) * 100));
 		batteryVoltage = (batADC / 255.0 * 30.0);
 		coolant = (tempCvt(table(cltADC, "thermfactor.inc") - 40));
 		egoVoltage = (egoADC / 255.0 * 5.0);
 		ego2Voltage = (fuelADC / 255.0 * 5.0);
 		mat = (tempCvt(table(matADC, "matfactor.inc") - 40));
 		rpm = (rpm100 * 100);
 		time = (timeNow());
 		if (CELSIUS)
 		{
 			egttemp = (egtADC * 3.90625);
 		}
 		else
 		{
 			egttemp = (egtADC * 7.15625);
 		}
 		if (NARROW_BAND_EGO)
 		{
 			afr2 = (table(fuelADC, "NBafr100.inc") / 100.0);
 			lambda2 = (afr2 / 14.7);
 		}
 		else if (ZEITRONIX_NON_LINEAR)
 		{
 			lambda2 = (table(fuelADC, "WBafr100Zeit.inc") / 100.0);
 			afr2 = (lambda2 * 14.7);
 		}
 		else if (INNOVATE_LC1_DEFAULT)
 		{
 			lambda2 = (fuelADC / 255.0 + 0.5);
 			afr2 = (lambda2 * 14.7);
 		}
 		else if (FAST_WIDEBAND_O2)
 		{
 			lambda2 = (fuelADC / 51);
 			afr2 = (lambda2 * 14.7);
 		}
 		else
 		{
 			lambda2 = (table(fuelADC, "WBlambda100MOT.inc") / 100.0);
 			afr2 = (lambda2 * 14.7);
 		}
 
 		if (MPXH6300A)
 		{
 			barometer = ((baroADC + 1.53) * 1.213675);
 			map = ((mapADC + 1.53) * 1.213675);
 		}
 		else if (MPXH6400A)
 		{
 			barometer = ((baroADC + 2.147) * 1.6197783);
 			map = ((mapADC + 2.147) * 1.6197783);
 		}
 		else if (MPX4250)
 		{
 			barometer = (table(baroADC, "kpafactor4250.inc"));
 			map = (table(mapADC, "kpafactor4250.inc"));
 		}
 		else
 		{
 			barometer = (table(baroADC, "kpafactor4115.inc"));
 			map = (table(mapADC, "kpafactor4115.inc"));
 		}
 		throttle = (table(tpsADC, "throttlefactor.inc"));
 		advSpark = ((advance * 0.352) - 10);
 		KnockAng = ((KnockAngle * 90 / 256));
 		KnockDeg = (-KnockAng);
 		CltIatAng = (CltIatAngle * 90 / 256);
 		CltIatDeg = ((CltIatAng < 45) ? CltIatAng : -90 + CltIatAng);
 		fuelvolt = ((fuelADC < 1) ? 0.0 : fuelADC * (5 / 255) - 0.5);
 		fuelpress = ((fuelADC < 1) ? 0.0 : fuelvolt / 0.04 + 1);
 		altDiv1 = (((alternate1) != 0) ? 2 : 1);
 		altDiv2 = (((alternate2) != 0) ? 2 : 1);
 		cycleTime1 = ((rpm < 100) ? 0 : 60000.0 / rpm * (2.0 - twoStroke1));
 		nSquirts1 = (nCylinders1 / divider1);
 		dutyCycle1 = ((rpm < 100) ? 0 : 100.0 * nSquirts1 / altDiv1 * pulseWidth1 / cycleTime1);
 		cycleTime2 = ((rpm < 100) ? 0 : 60000.0 / rpm * (2.0 - twoStroke2));
 		nSquirts2 = (nCylinders2 / divider2);
 		dutyCycle2 = ((rpm < 100) ? 0 : 100.0 * nSquirts2 / altDiv2 * pulseWidth2 / cycleTime2);
 		Open_Time1 = (1.0);
 		Open_Time2 = (1.0);
 		InjectorRating1 = (100);
 		InjectorRating2 = (100);
 		dutyCy1Real = ((rpm < 100) ? 0 : InjectorRating1 * nSquirts1 / altDiv1 * (pulseWidth1 - Open_Time1) / cycleTime1);
 		dutyCy2Real = ((rpm < 100) ? 0 : InjectorRating2 * nSquirts2 / altDiv2 * (pulseWidth2 - Open_Time2) / cycleTime2);
 		veCurr = (veCurr1);
 		pulseWidth = (pulseWidth1);
 		YOffset = (182);
 		XOffset = (187);
 		YForce = ((egtADC > YOffset + 1) ? (egtADC - YOffset) * 0.04464 : (egtADC < YOffset - 1) ? (egtADC - YOffset) * 0.04464 : 0);
 		XForce = ((fuelADC > XOffset + 1) ? (fuelADC - XOffset) * 0.04464 : (fuelADC < XOffset - 1) ? (fuelADC - XOffset) * 0.04464
 				: 0);
 		test = (mapDOT);
 		test2 = (egtADC);
 		iTimefull = ((iTimeX * 65536) + iTime);
 		RpmHitmp = ((iTimefull > 0) ? (60000000 * (2.0 - twoStroke1)) / (iTimefull * nCylinders1) : 0);
 		RpmHiRes = (int) ((RpmHitmp > 20) ? RpmHitmp : 0);
 		vacuum = ((barometer - map) * 0.2953007);
 		boost = ((map < barometer) ? 0.0 : (map - barometer) * 0.1450377);
 		boostVac = ((map < barometer) ? -vacuum : (map - barometer) * 0.1450377);
 		Speed = (70);
 		fuelCC = (2168);
 		squirtmul = ((divider1 < 2) ? 2 : (divider1 < 3) ? 1 : (divider1 < 5) ? 0.5 : (divider1 < 9) ? 0.25
 				: (divider1 < 17) ? 0.125 : 1);
 		squirts = ((alternate1 > 0) ? rpm100 / 0.6 * squirtmul : rpm100 * 2 * squirtmul / 0.6);
 		OpenWidth = (injOpen1 * 0.1);
 		CCpHr = ((pulseWidth < 1) ? 1 : (fuelCC / 60) * ((pulseWidth - OpenWidth) / 1000) * squirts * 3600);
 		mphTemp = ((fuelADC < 1) ? 0 : (fuelADC * Speed) / 127.5);
 		mph = ((mphTemp < 1) ? 0 : mphTemp);
 		USgph = (CCpHr / 3785);
 		USmpg = ((fuelADC < 1) ? 0 : (mph < 1) ? 0 : mph / USgph);
 		gph = (CCpHr / 4546);
 		mpg = ((fuelADC < 1) ? 0 : (mph < 1) ? 0 : mph / gph);
 		Cd = (0.33);
 		Mass = (1050);
 		Crr = (18);
 		Radius = (16.15);
 		GrTms = (0.88);
 		DiffRa = (3.31);
 		floodclear = ((tpsADC > 200) ? 1 : 0);
 		Timeroll = (portc & 4);
 		waterIlog = (porta & 16);
 		MAFVolts = (fuelADC * 0.0196078);
 		nosActive1 = ((((portd & 2)) != 0) ? 0 : 1);
 	}
 
 	@Override
 	public String getLogHeader()
 	{
 		StringBuffer b = new StringBuffer();
 
 		if (MSLVV_COMPATIBLE)
 		{
 			b.append("Time").append("\t");
 			b.append("SecL").append("\t");
 			b.append("RPM").append("\t");
 			b.append("MAP").append("\t");
 			b.append("TP").append("\t");
 			b.append("O2").append("\t");
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
 			b.append("Gve2").append("\t");
 			b.append("PW2").append("\t");
 			b.append("DutyCycle1").append("\t");
 			b.append("DutyCycle2").append("\t");
 			b.append("UserData1").append("\t");
 			b.append("UserData2").append("\t");
 			b.append("UserData3").append("\t");
 		}
 		else
 		{
 			b.append("Time").append("\t");
 			b.append("SecL").append("\t");
 			b.append("RPM/100").append("\t");
 			b.append("MAP").append("\t");
 			b.append("TP").append("\t");
 			b.append("O2").append("\t");
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
 			b.append("Gve2").append("\t");
 			b.append("PW2").append("\t");
 			b.append("DutyCycle1").append("\t");
 			b.append("DutyCycle2").append("\t");
 			b.append("idleDC").append("\t");
 			b.append("BCDuty3").append("\t");
 			b.append("Spark Angle").append("\t");
 			b.append("EGT").append("\t");
 			b.append("Fuel Press").append("\t");
 			b.append("Knock").append("\t");
 			b.append("RPM").append("\t");
 			b.append("barometer").append("\t");
 			b.append("porta").append("\t");
 			b.append("portb").append("\t");
 			b.append("portc").append("\t");
 			b.append("portd").append("\t");
 			b.append("NOS On").append("\t");
 			b.append("batt V").append("\t");
 		}
 		b.append(MSUtils.getLocationLogHeader());
 		return b.toString();
 	}
 
 	@Override
 	public String getLogRow()
 	{
 		StringBuffer b = new StringBuffer();
 
 		if (MSLVV_COMPATIBLE)
 		{
 			b.append(time).append("\t");
 			b.append(secl).append("\t");
 			b.append(rpm100).append("\t");
 			b.append(map).append("\t");
 			b.append(throttle).append("\t");
 			b.append(egoVoltage).append("\t");
 			b.append(mat).append("\t");
 			b.append(coolant).append("\t");
 			b.append(engine).append("\t");
 			b.append(egoCorrection).append("\t");
 			b.append(airCorrection).append("\t");
 			b.append(warmupEnrich).append("\t");
 			b.append(baroCorrection).append("\t");
 			b.append(gammaEnrich).append("\t");
 			b.append(accDecEnrich).append("\t");
 			b.append(veCurr1).append("\t");
 			b.append(round(pulseWidth1)).append("\t");
 			b.append(veCurr2).append("\t");
 			b.append(round(pulseWidth2)).append("\t");
 			b.append(dutyCycle1).append("\t");
 			b.append(dutyCycle2).append("\t");
 			b.append(round(pulseWidth2)).append("\t");
 			b.append(veCurr2).append("\t");
 			b.append(idleDC).append("\t");
 		}
 		else
 		{
 			b.append(time).append("\t");
 			b.append(secl).append("\t");
 			b.append(rpm100).append("\t");
 			b.append(map).append("\t");
 			b.append(throttle).append("\t");
 			b.append(egoVoltage).append("\t");
 			b.append(mat).append("\t");
 			b.append(coolant).append("\t");
 			b.append(engine).append("\t");
 			b.append(egoCorrection).append("\t");
 			b.append(airCorrection).append("\t");
 			b.append(warmupEnrich).append("\t");
 			b.append(baroCorrection).append("\t");
 			b.append(gammaEnrich).append("\t");
 			b.append(accDecEnrich).append("\t");
 			b.append(veCurr1).append("\t");
 			b.append(round(pulseWidth1)).append("\t");
 			b.append(veCurr2).append("\t");
 			b.append(round(pulseWidth2)).append("\t");
 			b.append(dutyCycle1).append("\t");
 			b.append(dutyCycle2).append("\t");
 			b.append(idleDC).append("\t");
 			b.append(round(bcDC)).append("\t");
 			b.append(advSpark).append("\t");
 			b.append(egttemp).append("\t");
 			b.append(fuelpress).append("\t");
 			b.append(KnockDeg).append("\t");
 			b.append(RpmHiRes).append("\t");
 			b.append(barometer).append("\t");
 			b.append(porta).append("\t");
 			b.append(portb).append("\t");
 			b.append(portc).append("\t");
 			b.append(portd).append("\t");
 			b.append(nosActive1).append("\t");
 			b.append(batteryVoltage).append("\t");
 		}
 		b.append(MSUtils.getLocationLogRow());
 		return b.toString();
 	}
 
 	@Override
 	public void initGauges()
 	{
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("deadGauge", "deadValue", "---", "", 0, 1, -1, -1, 2, 2, 0, 0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("baroADCGauge", "baroADC", "Barometer ADC", "", 0, 255, -1, -1, 256, 256,
 				0, 0, 0));
 		GaugeRegister.INSTANCE
 				.addGauge(new GaugeDetails("mapADCGauge", "mapADC", "MAP ADC", "", 0, 255, -1, -1, 256, 256, 0, 0, 0));
 		GaugeRegister.INSTANCE
 				.addGauge(new GaugeDetails("matADCGauge", "matADC", "MAT ADC", "", 0, 255, -1, -1, 256, 256, 0, 0, 0));
 		GaugeRegister.INSTANCE
 				.addGauge(new GaugeDetails("cltADCGauge", "cltADC", "CLT ADC", "", 0, 255, -1, -1, 256, 256, 0, 0, 0));
 		GaugeRegister.INSTANCE
 				.addGauge(new GaugeDetails("tpsADCGauge", "tpsADC", "TPS ADC", "", 0, 255, -1, -1, 256, 256, 0, 0, 0));
 		GaugeRegister.INSTANCE
 				.addGauge(new GaugeDetails("batADCGauge", "batADC", "BAT ADC", "", 0, 255, -1, -1, 256, 256, 0, 0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("egoGauge", "egoVoltage", "Rear Bank O2 Voltage", "volts", 0, 1.0, 0.2,
 				0.3, 0.6, 0.8, 2, 2, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("egoGauge", "egoVoltage", "Rear Bank O2 Voltage", "volts", 0.5, 4.5, 0.0,
 				0.0, 5.0, 5.0, 2, 2, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("ego2Gauge", "ego2Voltage", "Front Bank O2 Voltage", "volts", 0, 1.0, 0.2,
 				0.3, 0.6, 0.8, 2, 2, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("ego2Gauge", "ego2Voltage", "Front Bank O2 Voltage", "volts", 0.5, 4.5,
 				0.0, 0.0, 5.0, 5.0, 2, 2, 0));
 		GaugeRegister.INSTANCE
 				.addGauge(new GaugeDetails("lambdaGauge", "lambda", "Lambda", "", 0.5, 1.5, 0.5, 0.7, 2, 1.1, 2, 2, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("accelEnrichGauge", "accDecEnrich", "AE Driven Fuel Pct", "Pct", 100, 200,
 				0, 0, 999, 999, 0, 0, 0));
 		GaugeRegister.INSTANCE
				.addGauge(new GaugeDetails("afrGauge", "afr2", "Air:Fuel Ratio", "", 10, 19.4, 12, 13, 15, 16, 2, 2, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("clockGauge", "secl", "Clock", "Seconds", 0, 255, 10, 10, 245, 245, 0, 0,
 				0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("dutyCycle1Gauge", "dutyCycle1", "Duty Cycle 1", "%", 0, 100, -1, -1, 85,
 				90, 1, 1, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("dutyCycle2Gauge", "dutyCycle2", "Duty Cycle 2", "%", 0, 100, -1, -1, 85,
 				90, 1, 1, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("egoCorrGauge", "egoCorrection", "O2 Driven Correction (GEGO)", "%", 80,
 				120, 90, 99, 101, 110, 0, 0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("gammaEnrichGauge", "gammaEnrich", "Gamma Enrichment", "%", 50, 150, -1,
 				-1, 151, 151, 0, 0, 0));
 		if (MPXH6300A)
 		{
 			GaugeRegister.INSTANCE.addGauge(new GaugeDetails("mapGauge", "map", "Engine MAP", "kPa", 0, 304, 0, 20, 250, 275, 0, 0,
 					0));
 		}
 		else if (MPXH6400A)
 		{
 			GaugeRegister.INSTANCE.addGauge(new GaugeDetails("mapGauge", "map", "Engine MAP", "kPa", 0, 400, 0, 20, 250, 275, 0, 0,
 					0));
 		}
 		else
 		{
 			GaugeRegister.INSTANCE.addGauge(new GaugeDetails("mapGauge", "map", "Engine MAP", "kPa", 0, 255, 0, 20, 200, 245, 0, 0,
 					0));
 		}
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("baroGauge", "barometer", "Barometer", "kPa", 60, 110, 0, 20, 200, 245, 0,
 				0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("barocorrGauge", "baroCorrection", "baro correction", "%", 0, 120, -1, -1,
 				999, 999, 0, 0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("pulseWidth1Gauge", "pulseWidth1", "Pulse Width 1", "mSec", 0, 25.5, 1.0,
 				1.2, 20, 25, 1, 1, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("pulseWidth2Gauge", "pulseWidth2", "Pulse Width 2", "mSec", 0, 25.5, 1.0,
 				1.2, 20, 25, 1, 1, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("tachometer", "rpm", "Engine Speed", "RPM", 0, 8000, 300, 600, 3000, 5000,
 				0, 0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("throttleGauge", "throttle", "Throttle Position", "%TPS", 0, 100, -1, 1,
 				90, 100, 0, 0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("veBucketGauge", "veTuneValue", "VE Bucket", "%", 0, 150, -1, -1, 999,
 				999, 0, 0, 0));
 		GaugeRegister.INSTANCE
 				.addGauge(new GaugeDetails("veGauge", "veCurr", "VE Current", "%", 0, 120, -1, -1, 999, 999, 0, 0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("voltMeter", "batteryVoltage", "Battery Voltage", "volts", 7, 21, 8, 9,
 				15, 16, 2, 2, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("warmupEnrichGauge", "warmupEnrich", "Warmup Enrichment", "%", 100, 150,
 				-1, -1, 101, 105, 0, 0, 0));
 		if (CELSIUS)
 		{
 			GaugeRegister.INSTANCE.addGauge(new GaugeDetails("cltGauge", "coolant", "Coolant Temp", "%TEMP", -40, 120, -15, 0, 95,
 					105, 0, 0, 0));
 			GaugeRegister.INSTANCE.addGauge(new GaugeDetails("matGauge", "mat", "Manifold Air Temp", "%TEMP", -40, 110, -15, 0, 95,
 					100, 0, 0, 0));
 		}
 		else
 		{
 			GaugeRegister.INSTANCE.addGauge(new GaugeDetails("cltGauge", "coolant", "Coolant Temp", "%TEMP", -40, 250, 0, 0, 200,
 					220, 0, 0, 0));
 			GaugeRegister.INSTANCE.addGauge(new GaugeDetails("matGauge", "mat", "Manifold Air Temp", "%TEMP", -40, 215, 0, 0, 200,
 					210, 0, 0, 0));
 		}
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("advanceGauge", "advSpark", "Spark Advance", "deg BTDC", 50, -10, 0, 0,
 				35, 45, 1, 0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("stackGauge", "stackL", "CPU stack", "bytes", 240, 200, 0, 0, 254, 254, 0,
 				0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("fuelGauge", "fuelpress", "Fuel Pressure", "lb/in", 0, 80, 30, 40, 60, 65,
 				1, 1, 0));
 		if (CELSIUS)
 		{
 			GaugeRegister.INSTANCE
 					.addGauge(new GaugeDetails("egtGauge", "egttemp", "EGT", "C", 0, 1000, 0, 0, 1450, 1480, 1, 1, 0));
 		}
 		else
 		{
 			GaugeRegister.INSTANCE
 					.addGauge(new GaugeDetails("egtGauge", "egttemp", "EGT", "F", 0, 2200, 0, 0, 1450, 1480, 1, 1, 0));
 		}
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("egoCorrGauge2", "egoCorrection2", "EGO Correction2", "%", 50, 150, 90,
 				99, 101, 110, 0, 0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("testGauge", "test", "TEST", "", 0, 255, 255, 255, 0, 0, 0, 0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("test2Gauge", "test2", "TEST2", "", 0, 255, 255, 255, 0, 0, 0, 0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("AD6Gauge", "fuelADC", "AD6 raw", "", 0, 255, 255, 255, 0, 0, 0, 0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("AD7Gauge", "egtADC", "AD7 raw", "", 0, 255, 255, 255, 0, 0, 0, 0, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("lambdaGauge2", "lambda2", "Lambda2", "", 0.5, 1.5, 0.5, 0.7, 2, 1.1, 2,
 				2, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("afrGauge2", "afr2", "Air:Fuel Ratio2", "", 10, 19.4, 12, 13, 15, 16, 2,
 				2, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("vacuumGauge", "vacuum", "Engine Vacuum", "in-HG", 0, 30, 0, 0, 30, 30, 1,
 				1, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("boostgauge", "boost", "Engine Boost", "PSIG", 0, 20, 0, 0, 15, 20, 1, 1,
 				0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("VacBooGauge", "boostVac", "Engine Vac Boost", "in-HG/PSIG", -30, 30, -30,
 				-30, 30, 30, 1, 1, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("TargetAFRGauge", "TargetAFR", "Target AFR", "AFR", 10, 19.4, 0, 0, 20,
 				20, 2, 2, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("RealDutyGauge1", "dutyCy1Real", "Fuel Delivery 1", "Flow", 0, 75, -1, -1,
 				75, 75, 1, 1, 0));
 		GaugeRegister.INSTANCE.addGauge(new GaugeDetails("RealDutyGauge2", "dutyCy2Real", "Fuel Delivery 2", "Flow", 0, 75, -1, -1,
 				75, 75, 1, 1, 0));
 
 	}
 
 }
