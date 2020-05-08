 package uk.org.smithfamily.mslogger.ecuDef;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import android.content.Context;
 
 public class MS2Extra210 extends Megasquirt
 {
 
 	private static final String MS2_EXTRA_REL_2_1_0Q = "MS2Extra Rel 2.1.0q\0";
     private static final String MS2_EXTRA_REL_2_1_0P = "MS2Extra Rel 2.1.0p\0";
     private byte[]		ochCommand	= { 65 };
 	private byte[]		sigCommand	= { 81 };
 
 	// Runtime vars
 	int					seconds;
 	int					pulseWidth1;
 	int					pulseWidth2;
 	int					rpm;
 	int					advance;
 	int					squirt;
 	int					firing1;
 	int					firing2;
 	int					sched1;
 	int					inj1;
 	int					sched2;
 	int					inj2;
 	int					engine;
 	int					ready;
 	int					crank;
 	int					startw;
 	int					warmup;
 	int					tpsaccaen;
 	int					tpsaccden;
 	int					mapaccaen;
 	int					mapaccden;
 	int					afrtgt1;
 	int					afrtgt2;
 	int					wbo2_en1;
 	int					wbo2_en2;
 	int					barometer;
 	int					map;
 	int					mat;
 	int					coolant;
 	int					tps;
 	int					batteryVoltage;
 	int					afr1;
 	int					afr2;
 	int					knock;
 	int					egoCorrection1;
 	int					egoCorrection2;
 	int					airCorrection;
 	int					warmupEnrich;
 	int					accelEnrich;
 	int					tpsfuelcut;
 	int					baroCorrection;
 	int					gammaEnrich;
 	int					veCurr1;
 	int					veCurr2;
 	int					iacstep;
 	int					idleDC;
 	int					coldAdvDeg;
 	int					tpsDOT;
 	int					mapDOT;
 	int					dwell;
 	int					maf;
 	int					fuelload;
 	int					fuelCorrection;
 	int					portStatus;
 	int					port0;
 	int					port1;
 	int					port2;
 	int					port3;
 	int					port4;
 	int					port5;
 	int					port6;
 	int					knockRetard;
 	int					EAEFuelCorr1;
 	int					egoV;
 	int					egoV2;
 	int					status1;
 	int					status2;
 	int					status3;
 	int					status4;
 	int					looptime;
 	int					status5;
 	int					tpsADC;
 	int					fuelload2;
 	int					ignload;
 	int					ignload2;
 	int					synccnt;
 	int					timing_err;
 	int					deltaT;
 	int					wallfuel1;
 	int					gpioadc0;
 	int					gpioadc1;
 	int					gpioadc2;
 	int					gpioadc3;
 	int					gpioadc4;
 	int					gpioadc5;
 	int					gpioadc6;
 	int					gpioadc7;
 	int					gpiopwmin0;
 	int					gpiopwmin1;
 	int					gpiopwmin2;
 	int					gpiopwmin3;
 	int					adc6;
 	int					adc7;
 	int					wallfuel2;
 	int					EAEFuelCorr2;
 	int					boostduty;
 	int					syncreason;
 	int					user0;
 	int					gpioport0;
 	int					gpioport1;
 	int					gpioport2;
 	int					psaccaen;
 	int					psaccden;
 	int					ps;
 	int					psfuelcut;
 	int					psDOT;
 	int					psADC;
 	int					iming_err;
 
 	// Expressions
 	int					secl;
 	int					throttle;
 	int					pulseWidth;
 	double				lambda1;
 	double				lambda2;
 	int					egoCorrection;
 	int					veCurr;
 	int					accDecEnrich;
 	long				time;
 	double				rpm100;
 	double				altDiv1;
 	double				cycleTime1;
 	double				nSquirts1;
 	double				dutyCycle1;
 	double				cycleTime2;
 	double				nSquirts2;
 	double				dutyCycle2;
 	double				egoVoltage;
 	double				egt6temp;
 	double				egt7temp;
 	double				altDiv2;
 
 	// Constants
 	int					alternate;
 	int					twoStroke;
 	int					nCylinders;
 	int					divider;
 
 	// flags
 	boolean				NARROW_BAND_EGO;
 	boolean				LAMBDA;
 	boolean				EGTFULL;
 	boolean				CELSIUS;
 
 	private Set<String>	sigs		= new HashSet<String>(
 											Arrays.asList(new String[] { MS2_EXTRA_REL_2_1_0P, MS2_EXTRA_REL_2_1_0Q }));
 
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
 	public void calculate(byte[] ochBuffer) throws IOException
 	{
 		setupRuntime(ochBuffer);
 		try
 		{
 			throttle = tps;
 			secl = seconds % 256;// , "s" ; For runtime screen.
 			pulseWidth = pulseWidth1;// , "s" ; For runtime screen.
 			lambda1 = afr1 / 14.7;// , "Lambda"
 			lambda2 = afr2 / 14.7;// , "Lambda"
 			egoCorrection = (egoCorrection1 + egoCorrection2) / 2;// , "%" ;
 																	// Alias
 																	// for old
 																	// gauges.
 			veCurr = veCurr1;// , "%" ; For runtime display.
 			accDecEnrich = accelEnrich + ((tpsaccden != 0) ? tpsfuelcut : 100);// ,
 																				// "%"
 			time = System.currentTimeMillis();// , "s"
 			rpm100 = rpm / 100.0;//
 
 			altDiv1 = (alternate != 0) ? 2 : 1;//
 			altDiv2 = (alternate != 0) ? 2 : 1;//
 
 			cycleTime1 = 60000.0 / rpm * (2.0 - twoStroke);// , "ms"
 			nSquirts1 = nCylinders / divider;//
 			dutyCycle1 = 100.0 * nSquirts1 / altDiv1 * pulseWidth1 / cycleTime1;// ,
 																				// "%"
 
 			cycleTime2 = 60000.0 / rpm * (2.0 - twoStroke);// , "ms"
 			nSquirts2 = nCylinders / divider;//
 			dutyCycle2 = 100.0 * nSquirts2 / altDiv2 * pulseWidth2 / cycleTime2;// ,
 																				// "%"
 
 			if (NARROW_BAND_EGO)
 			{
 				egoVoltage = 1.0 - (afr1 * 0.04883);// , "V" ; For LED bars...
 
 			}
 			else if (LAMBDA)
 			{
 				egoVoltage = lambda1;// , "Lambda" ; For LED bars...
 			}
 			else
 			{
 				egoVoltage = afr1;// , "AFR" ; For LED bars...
 
 			}
 			if (EGTFULL)
 			{
 				if (CELSIUS)
 				{
 					egt6temp = adc6 * 1.222;// ; Setup for converting 0-5.01V =
 											// 0 -
 											// 1250C
 
 					egt7temp = adc7 * 1.222;// ; Setup for converting 0-5.01V =
 											// 0 -
 											// 1250C
 				}
 				else
 				{
 					egt6temp = (adc6 * 2.200) + 32;// ; Setup for converting
 													// 0-5.01V
 													// = 32 - 2282F
 					egt7temp = (adc7 * 2.200) + 32;// ; Setup for converting
 													// 0-5.01V
 													// = 32 - 2282F
 				}
 			}
 			else
 			// normal 0-1000 range
 			// With the 10K/10K circuit. 1000degC would apply 5.10V to the adc
 			// and
 			// result in '1044ADC counts' if that was possible
 			{
 				if (CELSIUS)
 				{
 					egt6temp = adc6 * 0.956;// ; Setup for converting 0-5.10V =
 											// 0 -
 											// 1000C
 					egt7temp = adc7 * 0.956;// ; Setup for converting 0-5.10V =
 											// 0 -
 											// 1000C
 				}
 				else
 				{
 					egt6temp = (adc6 * 1.721) + 32;// ; Setup for converting
 													// 0-5.10V
 													// = 32 - 1832F
 
 					egt7temp = (adc7 * 1.721) + 32;// ; Setup for converting
 													// 0-5.10V
 													// = 32 - 1832F
 				}
 			}
 		}
 		catch (Exception e)
 		{
 
 			// If we've got an arithmetic error, we've probably got duff
 			// constants.
 			throw new IOException(e.getLocalizedMessage());
 
 		}
 
 	}
 
 	private void setupRuntime(byte[] ochBuffer)
 	{
 		seconds = MSUtils.getWord(ochBuffer, 0);
 		pulseWidth1 = MSUtils.getWord(ochBuffer, 2);
 		pulseWidth2 = MSUtils.getWord(ochBuffer, 4);
 
 		rpm = MSUtils.getWord(ochBuffer, 6);
 		advance = MSUtils.getSignedWord(ochBuffer, 8);
 
 		squirt = MSUtils.getByte(ochBuffer, 10);
 		firing1 = MSUtils.getBits(ochBuffer, 10, 0, 0);
 		firing2 = MSUtils.getBits(ochBuffer, 10, 1, 1);
 		sched1 = MSUtils.getBits(ochBuffer, 10, 2, 2);
 		inj1 = MSUtils.getBits(ochBuffer, 10, 3, 3);
 		sched2 = MSUtils.getBits(ochBuffer, 10, 4, 4);
 		inj2 = MSUtils.getBits(ochBuffer, 10, 5, 5);
 
 		engine = MSUtils.getByte(ochBuffer, 11);
 		ready = MSUtils.getBits(ochBuffer, 11, 0, 0);
 		crank = MSUtils.getBits(ochBuffer, 11, 1, 1);
 		startw = MSUtils.getBits(ochBuffer, 11, 2, 2);
 		warmup = MSUtils.getBits(ochBuffer, 11, 3, 3);
 		psaccaen = MSUtils.getBits(ochBuffer, 11, 4, 4);
 		psaccden = MSUtils.getBits(ochBuffer, 11, 5, 5);
 		mapaccaen = MSUtils.getBits(ochBuffer, 11, 6, 6);
 		mapaccden = MSUtils.getBits(ochBuffer, 11, 7, 7);
 
 		afrtgt1 = MSUtils.getByte(ochBuffer, 12);
 		afrtgt2 = MSUtils.getByte(ochBuffer, 13);
 
 		wbo2_en1 = MSUtils.getByte(ochBuffer, 14);
 		wbo2_en2 = MSUtils.getByte(ochBuffer, 15);
 
 		barometer = MSUtils.getSignedWord(ochBuffer, 16);
 		map = MSUtils.getSignedWord(ochBuffer, 18);
 		mat = MSUtils.getSignedWord(ochBuffer, 20);
 		coolant = MSUtils.getSignedWord(ochBuffer, 22);
 		mat = MSUtils.getSignedWord(ochBuffer, 20);
 		coolant = MSUtils.getSignedWord(ochBuffer, 22);
 		ps = MSUtils.getSignedWord(ochBuffer, 24);
 		batteryVoltage = MSUtils.getSignedWord(ochBuffer, 26);
 		afr1 = MSUtils.getSignedWord(ochBuffer, 28);
 		afr2 = MSUtils.getSignedWord(ochBuffer, 30);
 		knock = MSUtils.getSignedWord(ochBuffer, 32);
 
 		egoCorrection1 = MSUtils.getSignedWord(ochBuffer, 34);
 		egoCorrection2 = MSUtils.getSignedWord(ochBuffer, 36);
 		airCorrection = MSUtils.getSignedWord(ochBuffer, 38);
 		warmupEnrich = MSUtils.getSignedWord(ochBuffer, 40);
 
 		accelEnrich = MSUtils.getSignedWord(ochBuffer, 42);
 		psfuelcut = MSUtils.getSignedWord(ochBuffer, 44);
 		baroCorrection = MSUtils.getSignedWord(ochBuffer, 46);
 		gammaEnrich = MSUtils.getSignedWord(ochBuffer, 48);
 
 		veCurr1 = MSUtils.getSignedWord(ochBuffer, 50);
 		veCurr2 = MSUtils.getSignedWord(ochBuffer, 52);
 		iacstep = MSUtils.getSignedWord(ochBuffer, 54);
 		idleDC = MSUtils.getSignedWord(ochBuffer, 54);
 		coldAdvDeg = MSUtils.getSignedWord(ochBuffer, 56);
 		psDOT = MSUtils.getSignedWord(ochBuffer, 58);
 		mapDOT = MSUtils.getSignedWord(ochBuffer, 60);
 		dwell = MSUtils.getWord(ochBuffer, 62);
 		maf = MSUtils.getSignedWord(ochBuffer, 64);
 		fuelload = MSUtils.getSignedWord(ochBuffer, 66);
 		fuelCorrection = MSUtils.getSignedWord(ochBuffer, 68);
 
 		portStatus = MSUtils.getByte(ochBuffer, 70);
 		port0 = MSUtils.getBits(ochBuffer, 70, 0, 0);
 		port1 = MSUtils.getBits(ochBuffer, 70, 1, 1);
 		port2 = MSUtils.getBits(ochBuffer, 70, 2, 2);
 		port3 = MSUtils.getBits(ochBuffer, 70, 3, 3);
 		port4 = MSUtils.getBits(ochBuffer, 70, 4, 4);
 		port5 = MSUtils.getBits(ochBuffer, 70, 5, 5);
 		port6 = MSUtils.getBits(ochBuffer, 70, 6, 6);
 
 		knockRetard = MSUtils.getByte(ochBuffer, 71);
 		EAEFuelCorr1 = MSUtils.getWord(ochBuffer, 72);
 		egoV = MSUtils.getSignedWord(ochBuffer, 74);
 		egoV2 = MSUtils.getSignedWord(ochBuffer, 76);
 		status1 = MSUtils.getByte(ochBuffer, 78);
 		status2 = MSUtils.getByte(ochBuffer, 79);
 		status3 = MSUtils.getByte(ochBuffer, 80);
 		status4 = MSUtils.getByte(ochBuffer, 81);
 		looptime = MSUtils.getWord(ochBuffer, 82);
 		status5 = MSUtils.getWord(ochBuffer, 84);
 		psADC = MSUtils.getWord(ochBuffer, 86);
 		fuelload2 = MSUtils.getWord(ochBuffer, 88);
 		ignload = MSUtils.getWord(ochBuffer, 90);
 		ignload2 = MSUtils.getWord(ochBuffer, 92);
 		synccnt = MSUtils.getByte(ochBuffer, 94);
 		iming_err = MSUtils.getSignedByte(ochBuffer, 95);
 		deltaT = MSUtils.getSignedLong(ochBuffer, 96);
 		wallfuel1 = MSUtils.getLong(ochBuffer, 100);
 
 		gpioadc0 = MSUtils.getWord(ochBuffer, 104);
 		gpioadc1 = MSUtils.getWord(ochBuffer, 106);
 		gpioadc2 = MSUtils.getWord(ochBuffer, 108);
 		gpioadc3 = MSUtils.getWord(ochBuffer, 110);
 		gpioadc4 = MSUtils.getWord(ochBuffer, 112);
 		gpioadc5 = MSUtils.getWord(ochBuffer, 114);
 		gpioadc6 = MSUtils.getWord(ochBuffer, 116);
 		gpioadc7 = MSUtils.getWord(ochBuffer, 118);
 
 		gpiopwmin0 = MSUtils.getWord(ochBuffer, 120);
 		gpiopwmin1 = MSUtils.getWord(ochBuffer, 122);
 		gpiopwmin2 = MSUtils.getWord(ochBuffer, 124);
 		gpiopwmin3 = MSUtils.getWord(ochBuffer, 126);
 		adc6 = MSUtils.getWord(ochBuffer, 128);
 		adc7 = MSUtils.getWord(ochBuffer, 130);
 		wallfuel2 = MSUtils.getLong(ochBuffer, 132);
 		EAEFuelCorr2 = MSUtils.getWord(ochBuffer, 136);
 		boostduty = MSUtils.getByte(ochBuffer, 138);
 		syncreason = MSUtils.getByte(ochBuffer, 139);
 		user0 = MSUtils.getWord(ochBuffer, 140);
 		gpioport0 = MSUtils.getByte(ochBuffer, 142);
 		gpioport1 = MSUtils.getByte(ochBuffer, 143);
 		gpioport2 = MSUtils.getByte(ochBuffer, 144);
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
 		b.append("Lost sync reason").append(MSUtils.getLocationLogHeader());
 		return b.toString();
 	}
 
 	@Override
 	public String getLogRow()
 	{
 		StringBuffer b = new StringBuffer();
 		b.append(time).append('\t');// , "Time", float, "%.3f"
 		b.append(seconds).append('\t');// , "SecL", int, "%d"
 		b.append(rpm).append('\t');// , "RPM", int, "%d"
 		b.append(map).append('\t');// , "MAP", float, "%.1f"
 		b.append(throttle).append('\t');// , "TP", int, "%d"
 		if (NARROW_BAND_EGO)
 		{
 			b.append(egoVoltage).append('\t');// , "O2", float, "%.3f"
 		}
 		else if (LAMBDA)
 		{
 			b.append(lambda1).append('\t');// , "Lambda", float, "%.3f"
 		}
 		else
 		{
 			b.append(afr1).append('\t');// , "AFR", float, "%.2f"
 		}
 
 		b.append(mat).append('\t');// , "MAT", float, "%.1f"
 		b.append(coolant).append('\t');// , "CLT", float, "%.1f"
 		b.append(engine).append('\t');// , "Engine", int, "%d"
 
 		b.append(egoCorrection).append('\t');// , "Gego", int, "%d"
 		b.append(airCorrection).append('\t');// , "Gair", int, "%d"
 		b.append(warmupEnrich).append('\t');// , "Gwarm", int, "%d"
 		b.append(baroCorrection).append('\t');// , "Gbaro", int, "%d"
 		b.append(gammaEnrich).append('\t');// , "Gammae", int, "%d"
 		b.append(accDecEnrich).append('\t');// , "TPSacc", int, "%d"
 
 		b.append(veCurr1).append('\t');// , "Gve", int, "%d"
 		b.append(pulseWidth1).append('\t');// , "PW", float, "%.3f"
 		b.append(dutyCycle1).append('\t');// , "DutyCycle1", float, "%.1f"
 
 		b.append(veCurr2).append('\t');// , "Gve2", int, "%d"
 		b.append(pulseWidth2).append('\t');// , "PW2", float, "%.3f"
 		b.append(dutyCycle2).append('\t');// , "DutyCycle2", float, "%.1f"
 
 		b.append(advance).append('\t');// , "SparkAdv", float, "%.1f"
 		b.append(knockRetard).append('\t');// , "knockRet", float, "%.1f"
 		b.append(coldAdvDeg).append('\t');// , "ColdAdv", float, "%.1f"
 		b.append(dwell).append('\t');// , "Dwell", float, "%.2f"
 		b.append(tpsDOT).append('\t');// , "tpsDOT", float, "%.1f"
 		b.append(mapDOT).append('\t');// , "mapDOT", float, "%.1f"
 		b.append(iacstep).append('\t');// , "IACstep", int, "%d"
 
 		b.append(batteryVoltage).append('\t');// , "Batt V", float, "%.1f"
 
 		b.append(deltaT).append('\t');// , "deltaT", float, "%.0f"
 		b.append(wallfuel1).append('\t');// , "WallFuel1", int, "%d"
 		b.append(wallfuel2).append('\t');// , "WallFuel2", int, "%d"
 		b.append(EAEFuelCorr1).append('\t');// , "EAE1 %", int, "%d"
 		b.append(EAEFuelCorr2).append('\t');// , "EAE2 %", int, "%d"
 		b.append(fuelload).append('\t');// , "Load", float, "%.1f"
 		b.append(fuelload2).append('\t');// , "Secondary Load", float, "%.1f"
 		b.append(ignload).append('\t');// , "Ign load", float, "%.1f"
 		b.append(ignload2).append('\t');// , "Secondary Ign Load", float, "%.1f"
 		b.append(egt6temp).append('\t');// , "EGT 6 temp", int, "%d"
 		b.append(egt7temp).append('\t');// , "EGT 7 temp", int, "%d"
 
 		b.append(gpioadc0).append('\t');// , "gpioadc0", int, "%d"
 		b.append(gpioadc1).append('\t');// , "gpioadc1", int, "%d"
 		b.append(gpioadc2).append('\t');// , "gpioadc2", int, "%d"
 		b.append(gpioadc3).append('\t');// , "gpioadc3", int, "%d"
 
 		b.append(status1).append('\t');// , "status1", int, "%d"
 		b.append(status2).append('\t');// , "status2", int, "%d"
 		b.append(status3).append('\t');// , "status3", int, "%d"
 		b.append(status4).append('\t');// , "status4", int, "%d"
 		b.append(status5).append('\t');// , "status5", int, "%d"
 		b.append(timing_err).append('\t');// , "timing err%", float, "%.1f"
 		b.append(afrtgt1).append('\t');// , "AFR Target 1", float, "%.1f"
 		b.append(boostduty).append('\t');// , "Boost Duty", int, "%d"
 		b.append(idleDC).append('\t');// , "PWM Idle Duty", float, "%.1f"
 		b.append(synccnt).append('\t');// , "Lost sync count", int, "%d"
 		b.append(syncreason).append(MSUtils.getLocationLogRow());// ,
 																	// "Lost sync reason",
 																	// int, "%d"
 
 		return b.toString();
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
		return throttle;
 	}
 
     @Override
     public int getSigSize()
     {
         return MS2_EXTRA_REL_2_1_0Q.length();
     }
 
 }
