 package com.limpidfox;
 
 import java.util.*;
 
 public class HP97 extends RPNCalculator
 {
 	private IDisplayUpdateHandler _displayHandler;
 
 	public void setDisplayHandler(IDisplayUpdateHandler handler) {
 		_displayHandler = handler;
 	}
 
 	private boolean _running;
 
 	public void SetRunning(boolean value) {
 		_running = value;
 	}
 
 	public boolean GetRunning() {
 		return _running;
 	}
 
 	public LinkedList<String> PrintQueue;
 	private boolean _powerOn;
 
 	public void SetPowerOn(boolean value) {
 		_powerOn = value;
 	}
 
 	public boolean GetPowerOn() {
 		return _powerOn;
 	}
 
 	private boolean _hp67PrintMode;
 
 	public void SetHP67PrintMode(boolean value) {
 		_hp67PrintMode = value;
 	}
 
 	public boolean GetHP67PrintMode() {
 		return _hp67PrintMode;
 	}
 
 	private boolean _stopPrint;
 
 	public void SetStopPrint(boolean value) {
 		_stopPrint = value;
 	}
 
 	public boolean GetStopPrint() {
 		return _stopPrint;
 	}
 
 	private String _cardFilename;
 
 	public void SetCardFilename(String value) {
 		_cardFilename = value;
 	}
 
 	public String GetCardFilename() {
 		return _cardFilename;
 	}
 
 	// Constants
 	private static final int MaxProgramSteps = 224;
 	private static final int MaxRegisters = 26;
 	private static final int MaxFlags = 4;
 	private static final int MaxNesting = 3;
 	private static final int SubroutineDelayMilli = 100;
 	private static final int ProgramStepDelayMilli = 20;
 	private static final int PauseTimeMilli = 1500;
 	private static final int KeyDelayMilli = 25;
 
 	// Private variables
 	private displayModes _displayMode;
 	private double _displayDigitCount;
 	private String _displayFixedFormat;
 	private String _displaySciFormat;
 	private String _displayEngFormat;
 	private String _pendingRegMath;
 	private ArrayList<Boolean> _flag = new ArrayList<Boolean>(4);
 	private boolean isFShift;
 	private String _pendingFunction;
 
 	private void SetPendingFunction(String value) {
 		_pendingFunction = value;
 	}
 
 	private String GetPendingFunction() {
 		return _pendingFunction;
 	}
 
 	private angleModes _angleMode;
 	private int _keyCnt = 0;
 	private int _gotoDigitCnt = 0;
 	private String _gotoStr = "";
 	private String[] key = new String[3];
 	private boolean _gotoFKeyLabel = false;
 	private boolean _branchExecuted = false;
 	private boolean _isRandomDisplay;
 	private String _randomDisplay;
 	private boolean decimalOn = true;
 	private Stack<Integer> NextStep;
 	private boolean _doPause;
 
 	private void SetDoPause(boolean value) {
 		_doPause = value;
 	}
 
 	private boolean GetDoPause() {
 		return _doPause;
 	}
 
 	private boolean _extendPause;
 
 	private void SetExtendPause(boolean value) {
 		_extendPause = value;
 	}
 
 	private boolean GetExtendPause() {
 		return _extendPause;
 	}
 
 	private int _highestStackCount = 0;
 	private ArrayList<Double> _register = new ArrayList<Double>(MaxRegisters);
 	private ArrayList<PgmInstruction> _program = new ArrayList<PgmInstruction>(
 			MaxProgramSteps + 1);
 	// private PgmInstruction[] _program = new PgmInstruction[MaxProgramSteps +
 	// 1];
 	public int _pgmStep = 0;
 
 	public displayModes GetDisplayMode() {
 		return _displayMode;
 	}
 
 	public angleModes GetAngleMode() {
 		return _angleMode;
 	}
 
 	public int GetDisplayDigitCount() {
 		return (int) _displayDigitCount;
 	}
 
 	// Private properties
 	private printModes _printMode;
 
 	public void SetPrintMode(printModes value) {
 		_printMode = value;
 	}
 
 	public printModes GetPrintMode() {
 		return _printMode;
 	}
 
 	public void ProcessKeyInput(String name)
 	{
 		SetStopPrint(true);
 		if (GetPowerOn() == false)
 		{
 			return;
 		}
 
 		// If RTN from keyboard - seed return step '000'
 		if (name.equals("RTN") && isFShift == false && GetRunMode() == true)
 		{
 			_pgmStep = 0;
 			autolift = true;
 			return;
 		}
 
 		if (GetRunning())
 		{
 			if (!GetDoPause())
 			{
 				// If we're not currently paused for
 				// display or input, then check key input
 				// to evaluate if we need to stop the program
 				if (!name.equals("RS"))
 				{
 					// Intent is to totally stop the program
 					// when any key but R/S is pressed during 
 					// program run.
 					SetRunning(false);
 					NextStep.clear();
 					_highestStackCount = 0;
 					DisplayUpdate();
 					return;
 				}
 			}
 			else
 			{
 				// A key was hit while we are pausing...
 				// let's process it and then extend pause...
 				// Not sure how that code looks yet - it may
 				// be we have to pass data to the "Pause" thread...
 				SetExtendPause(true);
 			}
 		}
 
 		// User Data Entry?
 		if (isPendingOperand == false)
 		{
 			if (name.equals("ZERO") || name.equals("ONE") || name.equals("TWO") || 
 			    name.equals("THREE") || name.equals("FOUR") || name.equals("FIVE") || 
 			    name.equals("SIX") || name.equals("SEVEN") || name.equals("EIGHT") || 
 			    name.equals("NINE") || name.equals("DEC"))
 			{
 					SetDataEntryFlag();
 			}
 		}
 		ProcessKeyStroke(name);
 		// Small delay to ley key process update internal state
 		// - such as current Display
 		try
 		{
 			Thread.sleep(KeyDelayMilli);
 		}
 		catch (Exception ex)
 		{}
 		
 	}	
 
 	@Override
 	public void ProcessKeyStroke(String name)
 	{
 		if (GetRunMode() == false)
 		{
 			ProcessProgramInput(name);
 			return;
 		}
 
 		if (isError)
 		{
 			ClearError();
 			return;
 		}
 
 		if (isFShift == false)
 		{
 			if (isPendingOperand)
 			{
 				if (ProcessPendingKey(name))
 				{
 					return;
 				}
 			}
 			else
 			{
 				if (ProcessUnshiftedKey(name))
 				{
 					return;
 				}
 			}
 		}
 		else
 		{
 			if (ProcessShiftedKey(name))
 			{
 				return;
 			}
 		}
 		isFShift = false;
 	}
 	
 	private boolean ProcessShiftedKey(String name)
 	{
 		if (isPendingOperand)
 		{
 			if (GetPendingFunction().equals("GTO"))
 			{
 				GotoLabel(name);
 			}
 
 			if (GetPendingFunction().equals("GSB"))
 			{
 				GoSubLabel(name);
 			}
 
 			if (GetPendingFunction().equals("STF") || GetPendingFunction().equals("CLF"))
 			{
 				if (ValidFlagKey(name))
 				{
 					int flagnum = 0;
 					if (name.equals("ZERO"))
 					{
 							flagnum = 0;
 					}
 					else if (name.equals("ONE"))
 					{
 							flagnum = 1;
 					}
 					else if (name.equals("TWO"))
 					{
 							flagnum = 2;
 					}
 					else if (name.equals("THREE"))
 					{
 							flagnum = 3;
 					}
 					if (GetPendingFunction().equals("STF"))
 					{
 							SetFlag(flagnum, true);
 					}
 					else if (GetPendingFunction().equals("CLF"))
 					{
 							SetFlag(flagnum, false);
 					}
 				}
 			}
 			SetPendingFunction("");
 			isPendingOperand = false;
 			_pendingRegMath = "";
 		}
 		else
 		{
 			if (name.equals("ONE"))
 			{
 					isFShift = false;
 					return true;
 			}
 			else if (name.equals("ENTER"))
 			{
 					PrintByMode("DEG");
 					_angleMode = angleModes.Degrees;
 					isFShift = false;
 					return true;
 			}
 			else if (name.equals("CHS"))
 			{
 					PrintByMode("RAD");
 					_angleMode = angleModes.Radians;
 					isFShift = false;
 					return true;
 			}
 			else if (name.equals("EEX"))
 			{
 					PrintByMode("GRD");
 					_angleMode = angleModes.Grads;
 					isFShift = false;
 					return true;
 			}
 			else if (name.equals("LBL"))
 			{
 					ProcessPending("STF");
 					return true;
 			}
 			else if (name.equals("GTO"))
 			{
 					ProcessPending("CLF");
 					return true;
 			}
 			ProcessOperation(name);
 			isEnteringExponent = false;
 		}
 		return false;
 	}	
 
 	private boolean ProcessUnshiftedKey(String name)
 	{
 		if (name.equals("F"))
 		{
 				isFShift = true;
 				return true;
 		}
 		else if (name.equals("EEX"))
 		{
 				if (isEnteringExponent)
 				{
 					return true;
 				}
 				if (isEnteringData)
 				{
 					isEnteringExponent = true;
 				}
 				else
 				{
 					if (autolift==true)
 					{
 						LiftUpStack();
 					}
 					isEnteringData = true;
 					isEnteringExponent = true;
 					_inputDigits = "1";
 					SetX(1);
 				}
 				autolift = true;
 				_exponentDigits = "00";
 				_exponentValue = 0;
 				_exponentSign = ' ';
 		}
 		else if (name.equals("ZERO"))
 		{
 				ProcessDigit(name);
 		}
 		else if (name.equals("ONE"))
 		{
 				ProcessDigit(name);
 		}
 		else if (name.equals("TWO"))
 		{
 				ProcessDigit(name);
 		}
 		else if (name.equals("THREE"))
 		{
 				ProcessDigit(name);
 		}
 		else if (name.equals("FOUR"))
 		{
 				ProcessDigit(name);
 		}
 		else if (name.equals("FIVE"))
 		{
 				ProcessDigit(name);
 		}
 		else if (name.equals("SIX"))
 		{
 				ProcessDigit(name);
 		}
 		else if (name.equals("SEVEN"))
 		{
 				ProcessDigit(name);
 		}
 		else if (name.equals("EIGHT"))
 		{
 				ProcessDigit(name);
 		}
 		else if (name.equals("NINE"))
 		{
 				ProcessDigit(name);
 		}
 		else if (name.equals("DEC"))
 		{
 				ProcessDigit(name);
 		}
 		else if (name.equals("STO"))
 		{
 				ProcessPending(name);
 				return true;
 		}
 		else if (name.equals("RCL"))
 		{
 				ProcessPending(name);
 				return true;
 		}
 		else if (name.equals("DSP"))
 		{
 				ProcessPending(name);
 				return true;
 		}
 		else if (name.equals("CHS"))
 		{
 				if (isEnteringExponent)
 				{
 					ProcessDigit(name);
 				}
 				else
 				{
 					ProcessOperation(name);
 				}
 		}
 		else
 		{
 				ProcessOperation(name);
 				isEnteringExponent = false;
 		}
 		return false;
 	}	
 
 	private boolean ProcessPendingKey(String name)
 	{
 		if (GetPendingFunction().equals("GTO"))
 		{
 			GotoLabel(name);
 			return true;
 		}
 
 		if (GetPendingFunction().equals("GSB"))
 		{
 			GoSub(name);
 			return true;
 		}
 
 		if (GetPendingFunction().equals("LBL"))
 		{
 			LabelKey(name);
 			return true;
 		}
 
 		if (GetPendingFunction().equals("DSP"))
 		{
 			if (ValidNumberKey(name))
 			{
 				SetSignificantDigitCount(name);
 			}
 			else
 			{
 				return true;
 			}
 		}
 
 		if (GetPendingFunction().equals("F?"))
 		{
 			if (name.equals("ZERO"))
 			{
 					CheckFlag(0);
 					return true;
 			}
 			else if (name.equals("ONE"))
 			{
 					CheckFlag(1);
 					return true;
 			}
 			else if (name.equals("TWO"))
 			{
 					CheckFlag(2);
 					return true;
 			}
 			else if (name.equals("THREE"))
 			{
 					CheckFlag(3);
 					return true;
 			}
 			else
 			{
 					SetPendingFunction("");
 					isPendingOperand = false;
 					ProcessKeyStroke(name);
 			}
 		}
 
 		if (GetPendingFunction().equals("DSZ"))
 		{
 			if (name.equals("IDX") || name.equals("RCLI"))
 			{
 				DecrementRegisterAndSkipIfZero(name);
 			}
 			else
 			{
 				SetPendingFunction("");
 				isPendingOperand = false;
 				return true;
 			}
 		}
 
 		if (GetPendingFunction().equals("ISZ"))
 		{
 			if (name.equals("IDX") || name.equals("RCLI"))
 			{
 				IncrementRegisterAndSkipIfZero(name);
 			}
 			else
 			{
 				SetPendingFunction("");
 				isPendingOperand = false;
 				return true;
 			}
 		}
 
 		if (ValidRegisterKey(name))
 		{
 			if (name.equals("MULT") || name.equals("DIV") || name.equals("PLUS") || name.equals("SUB"))
 			{
 					_pendingRegMath = name;
 					return true;
 			}
 			if (GetPendingFunction().equals("STO"))
 			{
 					Store(name);
 			}
 			else if (GetPendingFunction().equals("RCL"))
 			{
 					Recall(name);
 			}
 			isEnteringData = false;
 			ResetDigitFlags();
 		}
 
 		SetPendingFunction("");
 		isPendingOperand = false;
 		_pendingRegMath = "";
 		return false;
 	}	
 	
 	public void ProcessPending(String name)
 	{
 		if (isEnteringExponent)
 		{
 			SetX(GetX() * Math.pow(10, _exponentValue));
 			isEnteringExponent = false;
 			isEnteringData = false;
 		}
 
 		isPendingOperand = true;
 		SetPendingFunction(name);
 	}	
 	
 	public void ProcessOperation(String name)
 	{
 		if (isEnteringExponent)
 		{
 			SetX(GetX() * Math.pow(10, _exponentValue));
 		}
 
 		if (isFShift)
 		{
 			if (ProcessShiftedOperation(name))
 			{
 				return;
 			}
 		}
 		else
 		{
 			if (ProcessUnshiftedOperation(name))
 			{
 				return;
 			}
 		}
 		isEnteringData = false;
 		ResetDigitFlags();
 	}	
 	
 	private boolean ProcessUnshiftedOperation(String name)
 	{
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (name)
 //ORIGINAL LINE: case "A":
 		if (name.equals("A"))
 		{
 				GoSubShortcut(false, name);
 		}
 //ORIGINAL LINE: case "B":
 		else if (name.equals("B"))
 		{
 				GoSubShortcut(false, name);
 		}
 //ORIGINAL LINE: case "C":
 		else if (name.equals("C"))
 		{
 				GoSubShortcut(false, name);
 		}
 //ORIGINAL LINE: case "D":
 		else if (name.equals("D"))
 		{
 				GoSubShortcut(false, name);
 		}
 //ORIGINAL LINE: case "E":
 		else if (name.equals("E"))
 		{
 				GoSubShortcut(false, name);
 		}
 //ORIGINAL LINE: case "GTO":
 		else if (name.equals("GTO"))
 		{
 				GotoLabel("");
 				return true;
 		}
 //ORIGINAL LINE: case "GSB":
 		else if (name.equals("GSB"))
 		{
 				GoSubLabel("");
 				return true;
 		}
 //ORIGINAL LINE: case "RS":
 		else if (name.equals("RS"))
 		{
 				RunStop();
 				return true;
 		}
 //ORIGINAL LINE: case "RTN":
 		else if (name.equals("RTN"))
 		{
 				ReturnSub();
 				return true;
 		}
 //ORIGINAL LINE: case "IDX":
 		else if (name.equals("IDX"))
 		{
 				Recall("IDX");
 		}
 //ORIGINAL LINE: case "LBL":
 		else if (name.equals("LBL"))
 		{
 				LabelKey("");
 				return true;
 		}
 //ORIGINAL LINE: case "BST":
 		else if (name.equals("BST"))
 		{
 				BSTDecrementProgramStep();
 		}
 //ORIGINAL LINE: case "SST":
 		else if (name.equals("SST"))
 		{
 				SSTExecuteProgramStep();
 				return true;
 		}
 //ORIGINAL LINE: case "PLUS":
 		else if (name.equals("PLUS"))
 		{
 				Add();
 		}
 //ORIGINAL LINE: case "SUB":
 		else if (name.equals("SUB"))
 		{
 				Subtract();
 		}
 //ORIGINAL LINE: case "MULT":
 		else if (name.equals("MULT"))
 		{
 				Multiply();
 		}
 //ORIGINAL LINE: case "DIV":
 		else if (name.equals("DIV"))
 		{
 				Divide();
 		}
 //ORIGINAL LINE: case "ENTER":
 		else if (name.equals("ENTER"))
 		{
 				Enter();
 		}
 //ORIGINAL LINE: case "CLX":
 		else if (name.equals("CLX"))
 		{
 				ClearX();
 		}
 //ORIGINAL LINE: case "PRINT":
 		else if (name.equals("PRINT"))
 		{
 				PrintX();
 		}
 //ORIGINAL LINE: case "RDOWN":
 		else if (name.equals("RDOWN"))
 		{
 				RollDownStack();
 		}
 //ORIGINAL LINE: case "XY":
 		else if (name.equals("XY"))
 		{
 				SwapXY();
 		}
 //ORIGINAL LINE: case "RCLI":
 		else if (name.equals("RCLI"))
 		{
 				RecallI();
 		}
 //ORIGINAL LINE: case "FIX":
 		else if (name.equals("FIX"))
 		{
 				SetDisplayMode(name);
 		}
 //ORIGINAL LINE: case "SCI":
 		else if (name.equals("SCI"))
 		{
 				SetDisplayMode(name);
 		}
 //ORIGINAL LINE: case "ENG":
 		else if (name.equals("ENG"))
 		{
 				SetDisplayMode(name);
 		}
 //ORIGINAL LINE: case "CHS":
 		else if (name.equals("CHS"))
 		{
 				NegateX();
 				return true;
 		}
 //ORIGINAL LINE: case "SQR":
 		else if (name.equals("SQR"))
 		{
 				SquareRoot();
 		}
 //ORIGINAL LINE: case "SQUARE":
 		else if (name.equals("SQUARE"))
 		{
 				Square();
 		}
 //ORIGINAL LINE: case "RECIP":
 		else if (name.equals("RECIP"))
 		{
 				Reciprocal();
 		}
 //ORIGINAL LINE: case "PERC":
 		else if (name.equals("PERC"))
 		{
 				PercentOf();
 		}
 //ORIGINAL LINE: case "YX":
 		else if (name.equals("YX"))
 		{
 				YToXPower();
 		}
 //ORIGINAL LINE: case "SIN":
 		else if (name.equals("SIN"))
 		{
 				Sin();
 		}
 //ORIGINAL LINE: case "COS":
 		else if (name.equals("COS"))
 		{
 				Cos();
 		}
 //ORIGINAL LINE: case "TAN":
 		else if (name.equals("TAN"))
 		{
 				Tan();
 		}
 //ORIGINAL LINE: case "TOPOLAR":
 		else if (name.equals("TOPOLAR"))
 		{
 				ToPolar();
 		}
 //ORIGINAL LINE: case "TORECT":
 		else if (name.equals("TORECT"))
 		{
 				ToRect();
 		}
 //ORIGINAL LINE: case "LN":
 		else if (name.equals("LN"))
 		{
 				LN();
 		}
 //ORIGINAL LINE: case "EX":
 		else if (name.equals("EX"))
 		{
 				EXP();
 		}
 //ORIGINAL LINE: case "SUMPLUS":
 		else if (name.equals("SUMPLUS"))
 		{
 				SumPlus();
 		}
 		return false;
 	}
 
 	private boolean ProcessShiftedOperation(String name)
 	{
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (name)
 //ORIGINAL LINE: case "A":
 		if (name.equals("A"))
 		{
 				GoSubShortcut(true,name);
 		}
 //ORIGINAL LINE: case "B":
 		else if (name.equals("B"))
 		{
 				GoSubShortcut(true, name);
 		}
 //ORIGINAL LINE: case "C":
 		else if (name.equals("C"))
 		{
 				GoSubShortcut(true, name);
 		}
 //ORIGINAL LINE: case "D":
 		else if (name.equals("D"))
 		{
 				GoSubShortcut(true, name);
 		}
 //ORIGINAL LINE: case "E":
 		else if (name.equals("E"))
 		{
 				GoSubShortcut(true, name);
 				//begin conditional functions
 		}
 //ORIGINAL LINE: case "DEC":
 		else if (name.equals("DEC")) // MERGE
 		{
 			// TODO implement merge pgm
 				// MergePgmOrData();
 		}
 //ORIGINAL LINE: case "GSB":
 		else if (name.equals("GSB")) // F? -
 		{
 				CheckFlag(-1);
 		}
 //ORIGINAL LINE: case "BST":
 		else if (name.equals("BST")) // DSZ
 		{
 				DecrementRegisterAndSkipIfZero("");
 		}
 //ORIGINAL LINE: case "SST":
 		else if (name.equals("SST")) // ISZ
 		{
 				IncrementRegisterAndSkipIfZero("");
 		}
 //ORIGINAL LINE: case "ZERO":
 		else if (name.equals("ZERO")) //write data
 		{
 			// TODO implement writedata
 				//WriteData();
 		}
 //ORIGINAL LINE: case "ONE":
 		else if (name.equals("ONE"))
 		{
 				isFShift = false;
 				return true;
 		}
 //ORIGINAL LINE: case "FOUR":
 		else if (name.equals("FOUR")) // x<>0
 		{
 				XNotEqualZero();
 		}
 //ORIGINAL LINE: case "FIVE":
 		else if (name.equals("FIVE")) // x=0
 		{
 				XEqualZero();
 		}
 //ORIGINAL LINE: case "SIX":
 		else if (name.equals("SIX")) // x>0
 		{
 				XGreaterThanZero();
 		}
 //ORIGINAL LINE: case "SEVEN":
 		else if (name.equals("SEVEN")) // x<>y
 		{
 				XNotEqualY();
 		}
 //ORIGINAL LINE: case "EIGHT":
 		else if (name.equals("EIGHT")) // x=y
 		{
 				XEqualY();
 		}
 //ORIGINAL LINE: case "NINE":
 		else if (name.equals("NINE")) // x>y
 		{
 				XGreaterThanY();
 		}
 //ORIGINAL LINE: case "MULT":
 		else if (name.equals("MULT")) // x<=y
 		{
 				XLessThanEqualY();
 		}
 //ORIGINAL LINE: case "SUB":
 		else if (name.equals("SUB")) // x<0
 		{
 				XLessThanZero();
 				//end conditional functions
 		}
 //ORIGINAL LINE: case "FIX":
 		else if (name.equals("FIX"))
 		{
 				if (!GetHP67PrintMode())
 				{
 					RenderLine("");
 					Print();
 				}
 				return true;
 		}
 //ORIGINAL LINE: case "PRINT":
 		else if (name.equals("PRINT"))
 		{
 			Thread t = new Thread()
 			{
 				public void run()
 				{
 				    PrintStack();	
 				}
 			};
 			t.start();
 			//	PrintStack();
 		}
 //ORIGINAL LINE: case "SCI":
 		else if (name.equals("SCI"))
 		{
 				PrintProgram();
 		}
 //ORIGINAL LINE: case "ENG":
 		else if (name.equals("ENG"))
 		{
 			Thread t = new Thread ()
 			{
 				public void run()
 				{
 					PrintRegisters();
 				}
 			};
 			t.start();
 			//	PrintRegisters();
 		}
 //ORIGINAL LINE: case "RDOWN":
 		else if (name.equals("RDOWN"))
 		{
 				RollUpStack();
 		}
 //ORIGINAL LINE: case "DSP":
 		else if (name.equals("DSP"))
 		{
 				ShowLastX();
 		}
 //ORIGINAL LINE: case "TWO":
 		else if (name.equals("TWO"))
 		{
 				ClearRegisters();
 		}
 //ORIGINAL LINE: case "THREE":
 		else if (name.equals("THREE"))
 		{
 				ClearProgram();
 		}
 //ORIGINAL LINE: case "RECIP":
 		else if (name.equals("RECIP"))
 		{
 				Factorial();
 		}
 //ORIGINAL LINE: case "PERC":
 		else if (name.equals("PERC"))
 		{
 				PercentChg();
 		}
 //ORIGINAL LINE: case "YX":
 		else if (name.equals("YX"))
 		{
 				ABS();
 		}
 //ORIGINAL LINE: case "CLX":
 		else if (name.equals("CLX"))
 		{
 				SwapRegs();
 		}
 //ORIGINAL LINE: case "XY":
 		else if (name.equals("XY"))
 		{
 				SwapXI();
 		}
 //ORIGINAL LINE: case "RTN":
 		else if (name.equals("RTN"))
 		{
 				Round();
 		}
 //ORIGINAL LINE: case "TOPOLAR":
 		else if (name.equals("TOPOLAR"))
 		{
 				Int();
 		}
 //ORIGINAL LINE: case "TORECT":
 		else if (name.equals("TORECT"))
 		{
 				Frac();
 		}
 //ORIGINAL LINE: case "DIV":
 		else if (name.equals("DIV"))
 		{
 				PI();
 		}
 //ORIGINAL LINE: case "RCLI":
 		else if (name.equals("RCLI"))
 		{
 				RadToDeg();
 		}
 //ORIGINAL LINE: case "IDX":
 		else if (name.equals("IDX"))
 		{
 				DegToRad();
 		}
 //ORIGINAL LINE: case "SIN":
 		else if (name.equals("SIN"))
 		{
 				ArcSin();
 		}
 //ORIGINAL LINE: case "COS":
 		else if (name.equals("COS"))
 		{
 				ArcCos();
 		}
 //ORIGINAL LINE: case "TAN":
 		else if (name.equals("TAN"))
 		{
 				ArcTan();
 		}
 //ORIGINAL LINE: case "STO":
 		else if (name.equals("STO"))
 		{
 				ToHMS(true);
 		}
 //ORIGINAL LINE: case "RCL":
 		else if (name.equals("RCL"))
 		{
 				HMSTo(true);
 		}
 //ORIGINAL LINE: case "PLUS":
 		else if (name.equals("PLUS"))
 		{
 				HMSPlus();
 		}
 //ORIGINAL LINE: case "LN":
 		else if (name.equals("LN"))
 		{
 				LOG10();
 		}
 //ORIGINAL LINE: case "EX":
 		else if (name.equals("EX"))
 		{
 				TENX();
 		}
 //ORIGINAL LINE: case "SQUARE":
 		else if (name.equals("SQUARE"))
 		{
 				Mean();
 		}
 //ORIGINAL LINE: case "SQR":
 		else if (name.equals("SQR"))
 		{
 				StdDev();
 		}
 //ORIGINAL LINE: case "SUMPLUS":
 		else if (name.equals("SUMPLUS"))
 		{
 				SumMinus();
 		}
 //ORIGINAL LINE: case "RS":
 		else if (name.equals("RS"))
 		{
 				Pause();
 
 		}
 		return false;
 	}	
 
 	private void deleteInstruction()
 	{
 		if (_pgmStep == 0)
 		{
 			return;
 		}
 		for (int i = _pgmStep; i < MaxProgramSteps; i++)
 		{
 			_program.set(i, _program.get(i + 1));
 		}
 		_program.set(MaxProgramSteps, new PgmInstruction("R/S"));
 		_program.get(MaxProgramSteps).setKeyCode1("51");
 		_pgmStep -= 1;
 	}
 	
 	public void ProcessProgramInput(String name)
 	{
 		String keyCode = GetKeyCode(name);
 
 		if (_keyCnt == 0)
 		{
 			KeyCntZero(name,keyCode);
 		}
 		else
 		{
 			if (_keyCnt == 1)
 			{
 				KeyCntOne(name,keyCode);
 			}
 			else
 			{
 				if (_keyCnt >= 2)
 				{
 					KeyCntThree(name,keyCode);
 				}
 			}
 		}
 	}
 
 	public void KeyCntZero(String name, String keyCode)
 	{
 		// Process non-storable commands
 		if (name.equals("SST"))
 		{
 				if (_pgmStep == MaxProgramSteps || _pgmStep == 0)
 				{
 					_pgmStep = 1;
 				}
 				else
 				{
 					_pgmStep += 1;
 				}
 				return;
 		}
 		else if (name.equals("BST"))
 		{
 				if (_pgmStep == 1 || _pgmStep == 0)
 				{
 					_pgmStep = MaxProgramSteps;
 				}
 				else
 				{
 					_pgmStep -= 1;
 				}
 				return;
 		}
 
 		// Look for single key commands
 		SingleKey(keyCode);
 		if (_keyCnt != -1)
 		{
 			key[_keyCnt] = keyCode;
 		}
 		_keyCnt++;
 	}	
 
 	public void KeyCntOne(String name, String keyCode)
 	{
 		// Initiate "GTO .nnn" parsing
 		if (keyCode.equals("-62") && key[0].equals("22"))
 		{
 			_gotoDigitCnt = 1;
 			return;
 		}
 
 		if (key[0].equals("16"))
 		{
 			// See if DEL has been selected
 			if (keyCode.equals("01"))
 			{
 				deleteInstruction();
 				_keyCnt = 0;
 				return;
 			}
 			// CL PRGM
 			if (keyCode.equals("03"))
 			{
 				ClearProgram();
 				return;
 			}
 			// PRINT PGM
 			if (keyCode.equals("-12"))
 			{
 				PrintProgram();
 				_keyCnt = 0;
 				return;
 			}
 		}
 
 		if (_gotoDigitCnt > 0 && _gotoDigitCnt < 4)
 		{
 			if (keyCode.equals("00") || keyCode.equals("01") || keyCode.equals("02") || keyCode.equals("03") || keyCode.equals("04") || 
 				keyCode.equals("05") || keyCode.equals("06") || keyCode.equals("07") || keyCode.equals("08") || keyCode.equals("09"))
 			{
 				_gotoStr += String.format("%d", Integer.parseInt(keyCode));
 				_gotoDigitCnt++;
 				if (_gotoDigitCnt == 4)
 				{
 					int gotostep = Integer.parseInt(_gotoStr);
 					if (gotostep <= MaxProgramSteps)
 					{
 						_pgmStep = gotostep;
 					}
 					_gotoDigitCnt = 0;
 					_keyCnt = 0;
 					_gotoStr = "";
 					return;
 				}
 			}
 		}
 		else
 		{
 			// Check for two command sequence
 			if (key[0].equals("16"))
 			{
 				// Commands Starting with "F"
 				DoubleKey(keyCode);
 			}
 			if (key[0].equals("-63"))
 			{
 				DSPKey(keyCode);
 			}
 			if (key[0].equals("23"))
 			{
 				GSBKey(keyCode);
 			}
 			if (key[0].equals("22"))
 			{
 				GTOKey(keyCode);
 			}
 			if (key[0].equals("21"))
 			{
 				LBLKey(keyCode);
 			}
 			if (key[0].equals("36"))
 			{
 				RCLKey(keyCode);
 			}
 			if (key[0].equals("35"))
 			{
 				STOKey(keyCode);
 			}
 
 			// Store second key
 			if (_keyCnt != -1)
 			{
 				key[_keyCnt] = keyCode;
 			}
 			_keyCnt++;
 		}
 	}	
 	
 	public void KeyCntThree(String name, String keyCode)
 	{
 		// Check for three instruction sequence
 		if (key[0].equals("16") && key[1].equals("22"))
 		{
 			FCLFKey(keyCode);
 		}
 		if (key[0].equals("16") && key[1].equals("25"))
 		{
 			FDSZKey(keyCode);
 		}
 		if (key[0].equals("16") && key[1].equals("26"))
 		{
 			FISZKey(keyCode);
 		}
 		if (key[0].equals("16") && key[1].equals("23"))
 		{
 			CheckFlagKey(keyCode);
 		}
 		if (key[0].equals("16") && key[1].equals("21"))
 		{
 			FSTFKey(keyCode);
 		}
 		if (key[0].equals("23") && key[1].equals("16"))
 		{
 			FGSBKey(keyCode);
 		}
 		if (key[0].equals("22") && key[1].equals("16"))
 		{
 			FGTOKeys(keyCode);
 		}
 		if (key[0].equals("21") && key[1].equals("16"))
 		{
 			FLBLKeys(keyCode);
 		}
 		if (key[0].equals("35") && key[1].equals("-24"))
 		{
 			STOFuncKeys(keyCode);
 		}
 		if (key[0].equals("35") && key[1].equals("-45"))
 		{
 			STOFuncMinusKeys(keyCode);
 		}
 		if (key[0].equals("35") && key[1].equals("-55"))
 		{
 			STOFuncPlusKeys(keyCode);
 		}
 		if (key[0].equals("35") && key[1].equals("-35"))
 		{
 			STOFuncMultKeys(keyCode);
 		}
 		_keyCnt = 0;
 	}	
 	
 	public void DoubleKey(String keyCode)
 	{
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "11":
 		if (keyCode.equals("11"))
 		{
 				InsertInstruction("GSBa", keyCode, key[0], "23");
 		}
 //ORIGINAL LINE: case "12":
 		else if (keyCode.equals("12"))
 		{
 				InsertInstruction("GSBb", keyCode, key[0], "23");
 		}
 //ORIGINAL LINE: case "13":
 		else if (keyCode.equals("13"))
 		{
 				InsertInstruction("GSBc", keyCode, key[0], "23");
 		}
 //ORIGINAL LINE: case "14":
 		else if (keyCode.equals("14"))
 		{
 				InsertInstruction("GSBd", keyCode, key[0], "23");
 		}
 //ORIGINAL LINE: case "15":
 		else if (keyCode.equals("15"))
 		{
 				InsertInstruction("GSBe", keyCode, key[0], "23");
 		}
 //ORIGINAL LINE: case "33":
 		else if (keyCode.equals("33"))
 		{
 				InsertInstruction("10\u00aa", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "31":
 		else if (keyCode.equals("31"))
 		{
 				InsertInstruction("ABS", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("CLRG", "-53", key[0], "");
 		}
 //ORIGINAL LINE: case "42":
 		else if (keyCode.equals("42"))
 		{
 				InsertInstruction("COS\u00b9", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "-21":
 		else if (keyCode.equals("-21"))
 		{
 				InsertInstruction("DEG", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "44":
 		else if (keyCode.equals("44"))
 		{
 				InsertInstruction("FRC", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "45":
 		else if (keyCode.equals("45"))
 		{
 				InsertInstruction("D\u2192R", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "-23":
 		else if (keyCode.equals("-23"))
 		{
 				InsertInstruction("GRAD", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "35":
 		else if (keyCode.equals("35"))
 		{
 				InsertInstruction("\u2192HMS", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "36":
 		else if (keyCode.equals("36"))
 		{
 				InsertInstruction("HMS\u2192", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "-55":
 		else if (keyCode.equals("-55"))
 		{
 				InsertInstruction("HMS+", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "34":
 		else if (keyCode.equals("34"))
 		{
 				InsertInstruction("INT", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "32":
 		else if (keyCode.equals("32"))
 		{
 				InsertInstruction("LOG", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "-63":
 		else if (keyCode.equals("-63"))
 		{
 				InsertInstruction("LSTX", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "-62":
 		else if (keyCode.equals("-62"))
 		{
 				InsertInstruction("MRG", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "52":
 		else if (keyCode.equals("52"))
 		{
 				InsertInstruction("N!", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "55":
 		else if (keyCode.equals("55"))
 		{
 				InsertInstruction("%CH", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "-24":
 		else if (keyCode.equals("-24"))
 		{
 				InsertInstruction("P\u00a1", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "-13":
 		else if (keyCode.equals("-13"))
 		{
 				InsertInstruction("PREG", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "-14":
 		else if (keyCode.equals("-14"))
 		{
 				InsertInstruction("PRST", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "-51":
 		else if (keyCode.equals("-51"))
 		{
 				InsertInstruction("P\u2195S", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "51":
 		else if (keyCode.equals("51"))
 		{
 				InsertInstruction("PSE", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "-31":
 		else if (keyCode.equals("-31"))
 		{
 				InsertInstruction("R\u2191", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "-22":
 		else if (keyCode.equals("-22"))
 		{
 				InsertInstruction("RAD", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "46":
 		else if (keyCode.equals("46"))
 		{
 				InsertInstruction("R\u2192D", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "24":
 		else if (keyCode.equals("24"))
 		{
 				InsertInstruction("RND", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "54":
 		else if (keyCode.equals("54"))
 		{
 				InsertInstruction("S", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "56":
 		else if (keyCode.equals("56"))
 		{
 				InsertInstruction("\u03a3-", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "41":
 		else if (keyCode.equals("41"))
 		{
 				InsertInstruction("SIN\u00b9", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "-11":
 		else if (keyCode.equals("-11"))
 		{
 				InsertInstruction("SPC", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "43":
 		else if (keyCode.equals("43"))
 		{
 				InsertInstruction("TAN\u00b9", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "00":
 		else if (keyCode.equals("00"))
 		{
 				InsertInstruction("WDTA", "-61", key[0], "");
 		}
 //ORIGINAL LINE: case "04":
 		else if (keyCode.equals("04"))
 		{
 				InsertInstruction("X\u22600?", "-42", key[0], "");
 		}
 //ORIGINAL LINE: case "05":
 		else if (keyCode.equals("05"))
 		{
 				InsertInstruction("X=0?", "-43", key[0], "");
 		}
 //ORIGINAL LINE: case "06":
 		else if (keyCode.equals("06"))
 		{
 				InsertInstruction("X>0?", "-44", key[0], "");
 		}
 //ORIGINAL LINE: case "-45":
 		else if (keyCode.equals("-45"))
 		{
 				InsertInstruction("X<0?", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "07":
 		else if (keyCode.equals("07"))
 		{
 				InsertInstruction("X\u2260Y?", "-32", key[0], "");
 		}
 //ORIGINAL LINE: case "08":
 		else if (keyCode.equals("08"))
 		{
 				InsertInstruction("X=Y?", "-33", key[0], "");
 		}
 //ORIGINAL LINE: case "09":
 		else if (keyCode.equals("09"))
 		{
 				InsertInstruction("X>Y?", "-34", key[0], "");
 		}
 //ORIGINAL LINE: case "-35":
 		else if (keyCode.equals("-35"))
 		{
 				InsertInstruction("X\u2264Y?", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "53":
 		else if (keyCode.equals("53"))
 		{
 				InsertInstruction("x\u02c9", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "-41":
 		else if (keyCode.equals("-41"))
 		{
 				InsertInstruction("X\u2195I", keyCode, key[0], "");
 		}
 	}
 	
 	public void SingleKey(String keyCode)
 	{
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "11":
 		if (keyCode.equals("11"))
 		{
 				InsertInstruction("GSBA", keyCode, "23", "");
 		}
 //ORIGINAL LINE: case "12":
 		else if (keyCode.equals("12"))
 		{
 				InsertInstruction("GSBB", keyCode, "23", "");
 		}
 //ORIGINAL LINE: case "13":
 		else if (keyCode.equals("13"))
 		{
 				InsertInstruction("GSBC", keyCode, "23", "");
 		}
 //ORIGINAL LINE: case "14":
 		else if (keyCode.equals("14"))
 		{
 				InsertInstruction("GSBD", keyCode, "23", "");
 		}
 //ORIGINAL LINE: case "15":
 		else if (keyCode.equals("15"))
 		{
 				InsertInstruction("GSBE", keyCode, "23", "");
 		}
 //ORIGINAL LINE: case "00":
 		else if (keyCode.equals("00"))
 		{
 				InsertInstruction("0", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "01":
 		else if (keyCode.equals("01"))
 		{
 				InsertInstruction("1", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("2", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "03":
 		else if (keyCode.equals("03"))
 		{
 				InsertInstruction("3", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "04":
 		else if (keyCode.equals("04"))
 		{
 				InsertInstruction("4", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "05":
 		else if (keyCode.equals("05"))
 		{
 				InsertInstruction("5", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "06":
 		else if (keyCode.equals("06"))
 		{
 				InsertInstruction("6", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "07":
 		else if (keyCode.equals("07"))
 		{
 				InsertInstruction("7", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "08":
 		else if (keyCode.equals("08"))
 		{
 				InsertInstruction("8", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "09":
 		else if (keyCode.equals("09"))
 		{
 				InsertInstruction("9", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-62":
 		else if (keyCode.equals("-62"))
 		{
 				InsertInstruction(".", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "52":
 		else if (keyCode.equals("52"))
 		{
 				InsertInstruction("1/X", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-22":
 		else if (keyCode.equals("-22"))
 		{
 				InsertInstruction("CHS", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-51":
 		else if (keyCode.equals("-51"))
 		{
 				InsertInstruction("CLX", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "42":
 		else if (keyCode.equals("42"))
 		{
 				InsertInstruction("COS", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-24":
 		else if (keyCode.equals("-24"))
 		{
 				InsertInstruction("\u00f7 ", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-23":
 		else if (keyCode.equals("-23"))
 		{
 				InsertInstruction("EEX", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-13":
 		else if (keyCode.equals("-13"))
 		{
 				InsertInstruction("ENG", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-21":
 		else if (keyCode.equals("-21"))
 		{
 				InsertInstruction("ENT\u2191", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "33":
 		else if (keyCode.equals("33"))
 		{
 				InsertInstruction("e\u00aa", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-11":
 		else if (keyCode.equals("-11"))
 		{
 				InsertInstruction("FIX", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "32":
 		else if (keyCode.equals("32"))
 		{
 				InsertInstruction("LN", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-45":
 		else if (keyCode.equals("-45"))
 		{
 				InsertInstruction("- ", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "34":
 		else if (keyCode.equals("34"))
 		{
 				InsertInstruction("\u2192P", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "55":
 		else if (keyCode.equals("55"))
 		{
 				InsertInstruction("% ", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-55":
 		else if (keyCode.equals("-55"))
 		{
 				InsertInstruction("+ ", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-14":
 		else if (keyCode.equals("-14"))
 		{
 				InsertInstruction("PRTX", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "44":
 		else if (keyCode.equals("44"))
 		{
 				InsertInstruction("\u2192R", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-31":
 		else if (keyCode.equals("-31"))
 		{
 				InsertInstruction("R\u2193", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "51":
 		else if (keyCode.equals("51"))
 		{
 				InsertInstruction("R/S", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "24":
 		else if (keyCode.equals("24"))
 		{
 				InsertInstruction("RTN", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-12":
 		else if (keyCode.equals("-12"))
 		{
 				InsertInstruction("SCI", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "56":
 		else if (keyCode.equals("56"))
 		{
 				InsertInstruction("\u03a3+", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "41":
 		else if (keyCode.equals("41"))
 		{
 				InsertInstruction("SIN", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "54":
 		else if (keyCode.equals("54"))
 		{
 				InsertInstruction("\u221aX", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "43":
 		else if (keyCode.equals("43"))
 		{
 				InsertInstruction("TAN", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-35":
 		else if (keyCode.equals("-35"))
 		{
 				InsertInstruction("x ", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "53":
 		else if (keyCode.equals("53"))
 		{
 				InsertInstruction("X\u00b2", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "-41":
 		else if (keyCode.equals("-41"))
 		{
 				InsertInstruction("X\u2195Y", keyCode, "", "");
 		}
 //ORIGINAL LINE: case "31":
 		else if (keyCode.equals("31"))
 		{
 				InsertInstruction("Y\u00aa", keyCode, "", "");
 
 		}
 	}	
 	
 	public void DSPKey(String keyCode)
 	{
 		// Commands Starting with "DSP"
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "00":
 		if (keyCode.equals("00"))
 		{
 				InsertInstruction("DSP0", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "01":
 		else if (keyCode.equals("01"))
 		{
 				InsertInstruction("DSP1", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("DSP2", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "03":
 		else if (keyCode.equals("03"))
 		{
 				InsertInstruction("DSP3", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "04":
 		else if (keyCode.equals("04"))
 		{
 				InsertInstruction("DSP4", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "05":
 		else if (keyCode.equals("05"))
 		{
 				InsertInstruction("DSP5", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "06":
 		else if (keyCode.equals("06"))
 		{
 				InsertInstruction("DSP6", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "07":
 		else if (keyCode.equals("07"))
 		{
 				InsertInstruction("DSP7", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "08":
 		else if (keyCode.equals("08"))
 		{
 				InsertInstruction("DSP8", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "09":
 		else if (keyCode.equals("09"))
 		{
 				InsertInstruction("DSP9", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "45":
 		else if (keyCode.equals("45"))
 		{
 				InsertInstruction("DSP\u00a1", keyCode, key[0], "");
 		}
 	}
 
 	public void GSBKey(String keyCode)
 	{
 		// Commands Starting with "GSB"
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "00":
 		if (keyCode.equals("00"))
 		{
 				InsertInstruction("GSB0", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "01":
 		else if (keyCode.equals("01"))
 		{
 				InsertInstruction("GSB1", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("GSB2", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "03":
 		else if (keyCode.equals("03"))
 		{
 				InsertInstruction("GSB3", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "04":
 		else if (keyCode.equals("04"))
 		{
 				InsertInstruction("GSB4", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "05":
 		else if (keyCode.equals("05"))
 		{
 				InsertInstruction("GSB5", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "06":
 		else if (keyCode.equals("06"))
 		{
 				InsertInstruction("GSB6", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "07":
 		else if (keyCode.equals("07"))
 		{
 				InsertInstruction("GSB7", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "08":
 		else if (keyCode.equals("08"))
 		{
 				InsertInstruction("GSB8", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "09":
 		else if (keyCode.equals("09"))
 		{
 				InsertInstruction("GSB9", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "11":
 		else if (keyCode.equals("11"))
 		{
 				InsertInstruction("GSBA", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "12":
 		else if (keyCode.equals("12"))
 		{
 				InsertInstruction("GSBB", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "13":
 		else if (keyCode.equals("13"))
 		{
 				InsertInstruction("GSBC", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "14":
 		else if (keyCode.equals("14"))
 		{
 				InsertInstruction("GSBD", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "15":
 		else if (keyCode.equals("15"))
 		{
 				InsertInstruction("GSBE", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "45":
 		else if (keyCode.equals("45"))
 		{
 				InsertInstruction("GSB\u00a1", keyCode, key[0], "");
 		}
 	}
 
 	public void GTOKey(String keyCode)
 	{
 		// Commands Starting with "GTO"
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "00":
 		if (keyCode.equals("00"))
 		{
 				InsertInstruction("GTO0", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "01":
 		else if (keyCode.equals("01"))
 		{
 				InsertInstruction("GTO1", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("GTO2", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "03":
 		else if (keyCode.equals("03"))
 		{
 				InsertInstruction("GTO3", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "04":
 		else if (keyCode.equals("04"))
 		{
 				InsertInstruction("GTO4", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "05":
 		else if (keyCode.equals("05"))
 		{
 				InsertInstruction("GTO5", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "06":
 		else if (keyCode.equals("06"))
 		{
 				InsertInstruction("GTO6", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "07":
 		else if (keyCode.equals("07"))
 		{
 				InsertInstruction("GTO7", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "08":
 		else if (keyCode.equals("08"))
 		{
 				InsertInstruction("GTO8", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "09":
 		else if (keyCode.equals("09"))
 		{
 				InsertInstruction("GTO9", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "11":
 		else if (keyCode.equals("11"))
 		{
 				InsertInstruction("GTOA", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "12":
 		else if (keyCode.equals("12"))
 		{
 				InsertInstruction("GTOB", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "13":
 		else if (keyCode.equals("13"))
 		{
 				InsertInstruction("GTOC", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "14":
 		else if (keyCode.equals("14"))
 		{
 				InsertInstruction("GTOD", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "15":
 		else if (keyCode.equals("15"))
 		{
 				InsertInstruction("GTOE", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "45":
 		else if (keyCode.equals("45"))
 		{
 				InsertInstruction("GTO\u00a1", keyCode, key[0], "");
 		}
 	}	
 	
 	public void LBLKey(String keyCode)
 	{
 		// Commands Starting with "LBL"
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "00":
 		if (keyCode.equals("00"))
 		{
 				InsertInstruction("*LBL0", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "01":
 		else if (keyCode.equals("01"))
 		{
 				InsertInstruction("*LBL1", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("*LBL2", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "03":
 		else if (keyCode.equals("03"))
 		{
 				InsertInstruction("*LBL3", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "04":
 		else if (keyCode.equals("04"))
 		{
 				InsertInstruction("*LBL4", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "05":
 		else if (keyCode.equals("05"))
 		{
 				InsertInstruction("*LBL5", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "06":
 		else if (keyCode.equals("06"))
 		{
 				InsertInstruction("*LBL6", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "07":
 		else if (keyCode.equals("07"))
 		{
 				InsertInstruction("*LBL7", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "08":
 		else if (keyCode.equals("08"))
 		{
 				InsertInstruction("*LBL8", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "09":
 		else if (keyCode.equals("09"))
 		{
 				InsertInstruction("*LBL9", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "11":
 		else if (keyCode.equals("11"))
 		{
 				InsertInstruction("*LBLA", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "12":
 		else if (keyCode.equals("12"))
 		{
 				InsertInstruction("*LBLB", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "13":
 		else if (keyCode.equals("13"))
 		{
 				InsertInstruction("*LBLC", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "14":
 		else if (keyCode.equals("14"))
 		{
 				InsertInstruction("*LBLD", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "15":
 		else if (keyCode.equals("15"))
 		{
 				InsertInstruction("*LBLE", keyCode, key[0], "");
 		}
 	}
 
 	public void RCLKey(String keyCode)
 	{
 		// Commands Starting with "RCL"
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "00":
 		if (keyCode.equals("00"))
 		{
 				InsertInstruction("RCL0", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "01":
 		else if (keyCode.equals("01"))
 		{
 				InsertInstruction("RCL1", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("RCL2", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "03":
 		else if (keyCode.equals("03"))
 		{
 				InsertInstruction("RCL3", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "04":
 		else if (keyCode.equals("04"))
 		{
 				InsertInstruction("RCL4", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "05":
 		else if (keyCode.equals("05"))
 		{
 				InsertInstruction("RCL5", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "06":
 		else if (keyCode.equals("06"))
 		{
 				InsertInstruction("RCL6", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "07":
 		else if (keyCode.equals("07"))
 		{
 				InsertInstruction("RCL7", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "08":
 		else if (keyCode.equals("08"))
 		{
 				InsertInstruction("RCL8", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "09":
 		else if (keyCode.equals("09"))
 		{
 				InsertInstruction("RCL9", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "11":
 		else if (keyCode.equals("11"))
 		{
 				InsertInstruction("RCLA", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "12":
 		else if (keyCode.equals("12"))
 		{
 				InsertInstruction("RCLB", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "13":
 		else if (keyCode.equals("13"))
 		{
 				InsertInstruction("RCLC", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "14":
 		else if (keyCode.equals("14"))
 		{
 				InsertInstruction("RCLD", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "15":
 		else if (keyCode.equals("15"))
 		{
 				InsertInstruction("RCLE", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "46":
 		else if (keyCode.equals("46"))
 		{
 				InsertInstruction("RCLI", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "56":
 		else if (keyCode.equals("56"))
 		{
 				InsertInstruction("RCL\u03a3", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "45":
 		else if (keyCode.equals("45"))
 		{
 				InsertInstruction("RCL\u00a1", keyCode, key[0], "");
 		}
 	}
 
 	public void STOKey(String keyCode)
 	{
 		// Commands Starting with "STO"
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "00":
 		if (keyCode.equals("00"))
 		{
 				InsertInstruction("STO0", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "01":
 		else if (keyCode.equals("01"))
 		{
 				InsertInstruction("STO1", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("STO2", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "03":
 		else if (keyCode.equals("03"))
 		{
 				InsertInstruction("STO3", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "04":
 		else if (keyCode.equals("04"))
 		{
 				InsertInstruction("STO4", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "05":
 		else if (keyCode.equals("05"))
 		{
 				InsertInstruction("STO5", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "06":
 		else if (keyCode.equals("06"))
 		{
 				InsertInstruction("STO6", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "07":
 		else if (keyCode.equals("07"))
 		{
 				InsertInstruction("STO7", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "08":
 		else if (keyCode.equals("08"))
 		{
 				InsertInstruction("STO8", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "09":
 		else if (keyCode.equals("09"))
 		{
 				InsertInstruction("STO9", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "11":
 		else if (keyCode.equals("11"))
 		{
 				InsertInstruction("STOA", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "12":
 		else if (keyCode.equals("12"))
 		{
 				InsertInstruction("STOB", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "13":
 		else if (keyCode.equals("13"))
 		{
 				InsertInstruction("STOC", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "14":
 		else if (keyCode.equals("14"))
 		{
 				InsertInstruction("STOD", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "15":
 		else if (keyCode.equals("15"))
 		{
 				InsertInstruction("STOE", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "45":
 		else if (keyCode.equals("45"))
 		{
 				InsertInstruction("STO\u00a1", keyCode, key[0], "");
 		}
 //ORIGINAL LINE: case "46":
 		else if (keyCode.equals("46"))
 		{
 				InsertInstruction("STOI", keyCode, key[0], "");
 		}
 	}	
 	
 	public void FCLFKey(String keyCode)
 	{
 		// Commands Starting with "F" "CLF"
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "00":
 		if (keyCode.equals("00"))
 		{
 				InsertInstruction("CF0", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "01":
 		else if (keyCode.equals("01"))
 		{
 				InsertInstruction("CF1", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("CF2", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "03":
 		else if (keyCode.equals("03"))
 		{
 				InsertInstruction("CF3", keyCode, key[1], key[0]);
 		}
 	}
 
 	public void CheckFlagKey(String keyCode)
 	{
 		// Commands Starting with "F" "F?"
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "00":
 		if (keyCode.equals("00"))
 		{
 				InsertInstruction("F0?", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "01":
 		else if (keyCode.equals("01"))
 		{
 				InsertInstruction("F1?", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("F2?", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "03":
 		else if (keyCode.equals("03"))
 		{
 				InsertInstruction("F3?", keyCode, key[1], key[0]);
 		}
 	}
 
 	public void FDSZKey(String keyCode)
 	{
 		// Commands Starting with "F" "DSZ"
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "46":
 		if (keyCode.equals("46"))
 		{
 				InsertInstruction("DSZI", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "45":
 		else if (keyCode.equals("45"))
 		{
 				InsertInstruction("DSZ\u00a1", keyCode, key[1], key[0]);
 		}
 	}
 
 	public void FISZKey(String keyCode)
 	{
 		// Commands Starting with "F" "ISZ"
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "46":
 		if (keyCode.equals("46"))
 		{
 				InsertInstruction("ISZI", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "45":
 		else if (keyCode.equals("45"))
 		{
 				InsertInstruction("ISZ\u00a1", keyCode, key[1], key[0]);
 		}
 	}
 
 	public void FSTFKey(String keyCode)
 	{
 		// Commands Starting with "F" "STF"
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "00":
 		if (keyCode.equals("00"))
 		{
 				InsertInstruction("SF0", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "01":
 		else if (keyCode.equals("01"))
 		{
 				InsertInstruction("SF1", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("SF2", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "03":
 		else if (keyCode.equals("03"))
 		{
 				InsertInstruction("SF3", keyCode, key[1], key[0]);
 		}
 
 	}
 
 	public void FGSBKey(String keyCode)
 	{
 		// Commands Starting with "GSB" "F"
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "11":
 		if (keyCode.equals("11"))
 		{
 				InsertInstruction("GSBa", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "12":
 		else if (keyCode.equals("12"))
 		{
 				InsertInstruction("GSBb", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "13":
 		else if (keyCode.equals("13"))
 		{
 				InsertInstruction("GSBc", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "14":
 		else if (keyCode.equals("14"))
 		{
 				InsertInstruction("GSBd", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "15":
 		else if (keyCode.equals("15"))
 		{
 				InsertInstruction("GSBe", keyCode, key[1], key[0]);
 		}
 	}	
 	
 	public void FGTOKeys(String keyCode)
 	{
 		// Commands Starting with "GTO"
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "11":
 		if (keyCode.equals("11"))
 		{
 				InsertInstruction("GTOa", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "12":
 		else if (keyCode.equals("12"))
 		{
 				InsertInstruction("GTOb", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "13":
 		else if (keyCode.equals("13"))
 		{
 				InsertInstruction("GTOc", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "14":
 		else if (keyCode.equals("14"))
 		{
 				InsertInstruction("GTOd", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "15":
 		else if (keyCode.equals("15"))
 		{
 				InsertInstruction("GTOe", keyCode, key[1], key[0]);
 		}
 
 	}
 
 	public void FLBLKeys(String keyCode)
 	{
 		// Commands Starting with "LBL"
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "11":
 		if (keyCode.equals("11"))
 		{
 				InsertInstruction("*LBLa", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "12":
 		else if (keyCode.equals("12"))
 		{
 				InsertInstruction("*LBLb", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "13":
 		else if (keyCode.equals("13"))
 		{
 				InsertInstruction("*LBLc", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "14":
 		else if (keyCode.equals("14"))
 		{
 				InsertInstruction("*LBLd", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "15":
 		else if (keyCode.equals("15"))
 		{
 				InsertInstruction("*LBLe", keyCode, key[1], key[0]);
 		}
 
 	}
 
 	public void STOFuncKeys(String keyCode)
 	{
 		// Commands Starting with "STO" /, +, -, or *
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "00":
 		if (keyCode.equals("00"))
 		{
 				InsertInstruction("ST\u00f70", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "01":
 		else if (keyCode.equals("01"))
 		{
 				InsertInstruction("ST\u00f71", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("ST\u00f72", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "03":
 		else if (keyCode.equals("03"))
 		{
 				InsertInstruction("ST\u00f73", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "04":
 		else if (keyCode.equals("04"))
 		{
 				InsertInstruction("ST\u00f74", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "05":
 		else if (keyCode.equals("05"))
 		{
 				InsertInstruction("ST\u00f75", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "06":
 		else if (keyCode.equals("06"))
 		{
 				InsertInstruction("ST\u00f76", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "07":
 		else if (keyCode.equals("07"))
 		{
 				InsertInstruction("ST\u00f77", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "08":
 		else if (keyCode.equals("08"))
 		{
 				InsertInstruction("ST\u00f78", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "09":
 		else if (keyCode.equals("09"))
 		{
 				InsertInstruction("ST\u00f79", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "45":
 		else if (keyCode.equals("45"))
 		{
 				InsertInstruction("ST\u00f7\u00a1", keyCode, key[1], key[0]);
 		}
 
 	}
 
 	public void STOFuncMinusKeys(String keyCode)
 	{
 		// Commands Starting with "STO" /, +, -, or *
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "00":
 		if (keyCode.equals("00"))
 		{
 				InsertInstruction("ST-0", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "01":
 		else if (keyCode.equals("01"))
 		{
 				InsertInstruction("ST-1", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("ST-2", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "03":
 		else if (keyCode.equals("03"))
 		{
 				InsertInstruction("ST-3", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "04":
 		else if (keyCode.equals("04"))
 		{
 				InsertInstruction("ST-4", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "05":
 		else if (keyCode.equals("05"))
 		{
 				InsertInstruction("ST-5", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "06":
 		else if (keyCode.equals("06"))
 		{
 				InsertInstruction("ST-6", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "07":
 		else if (keyCode.equals("07"))
 		{
 				InsertInstruction("ST-7", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "08":
 		else if (keyCode.equals("08"))
 		{
 				InsertInstruction("ST-8", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "09":
 		else if (keyCode.equals("09"))
 		{
 				InsertInstruction("ST-9", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "45":
 		else if (keyCode.equals("45"))
 		{
 				InsertInstruction("ST-\u00a1", keyCode, key[1], key[0]);
 		}
 
 	}	
 	
 	public void STOFuncPlusKeys(String keyCode)
 	{
 		// Commands Starting with "STO" /, +, -, or *
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "00":
 		if (keyCode.equals("00"))
 		{
 				InsertInstruction("ST+0", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "01":
 		else if (keyCode.equals("01"))
 		{
 				InsertInstruction("ST+1", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("ST+2", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "03":
 		else if (keyCode.equals("03"))
 		{
 				InsertInstruction("ST+3", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "04":
 		else if (keyCode.equals("04"))
 		{
 				InsertInstruction("ST+4", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "05":
 		else if (keyCode.equals("05"))
 		{
 				InsertInstruction("ST+5", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "06":
 		else if (keyCode.equals("06"))
 		{
 				InsertInstruction("ST+6", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "07":
 		else if (keyCode.equals("07"))
 		{
 				InsertInstruction("ST+7", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "08":
 		else if (keyCode.equals("08"))
 		{
 				InsertInstruction("ST+8", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "09":
 		else if (keyCode.equals("09"))
 		{
 				InsertInstruction("ST+9", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "45":
 		else if (keyCode.equals("45"))
 		{
 				InsertInstruction("ST+\u00a1", keyCode, key[1], key[0]);
 		}
 
 	}
 
 	public void STOFuncMultKeys(String keyCode)
 	{
 		// Commands Starting with "STO" /, +, -, or *
 //C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
 //		switch (keyCode)
 //ORIGINAL LINE: case "00":
 		if (keyCode.equals("00"))
 		{
 				InsertInstruction("STx0", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "01":
 		else if (keyCode.equals("01"))
 		{
 				InsertInstruction("STx1", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "02":
 		else if (keyCode.equals("02"))
 		{
 				InsertInstruction("STx2", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "03":
 		else if (keyCode.equals("03"))
 		{
 				InsertInstruction("STx3", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "04":
 		else if (keyCode.equals("04"))
 		{
 				InsertInstruction("STx4", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "05":
 		else if (keyCode.equals("05"))
 		{
 				InsertInstruction("STx5", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "06":
 		else if (keyCode.equals("06"))
 		{
 				InsertInstruction("STx6", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "07":
 		else if (keyCode.equals("07"))
 		{
 				InsertInstruction("STx7", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "08":
 		else if (keyCode.equals("08"))
 		{
 				InsertInstruction("STx8", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "09":
 		else if (keyCode.equals("09"))
 		{
 				InsertInstruction("STx9", keyCode, key[1], key[0]);
 		}
 //ORIGINAL LINE: case "45":
 		else if (keyCode.equals("45"))
 		{
 				InsertInstruction("STx\u00a1", keyCode, key[1], key[0]);
 		}
 
 	}	
 	
 	
 	
 	public void GotoLabel(String name)
 	{
 		if (name.length() == 0)
 		{
 			SetPendingFunction("GTO");
 			isPendingOperand = true;
 			_gotoFKeyLabel = false;
 		}
 		else
 		{
 			String keyCode = GetKeyCode(name);
 			if (keyCode.equals("-62"))
 			{
 				// Initiate "GTO .nnn" parsing
 				_gotoDigitCnt = 1;
 				return;
 			}
 			if (_gotoDigitCnt > 0 && _gotoDigitCnt < 4)
 			{
 				GetGTODigits(keyCode);
 			}
 			else
 			{
 			if (name.equals("F"))
 			{
 				_gotoFKeyLabel = true;
 				return;
 			}
 			else
 			{
 				ProcessGTOLabel(name);
 				return;
 			}
 			}
 		}
 	}	
 
 	private void GetGTODigits(String keyCode)
 	{
 		if (keyCode.equals("00") || keyCode.equals("01") || keyCode.equals("02") || keyCode.equals("03") || keyCode.equals("04") ||
 			keyCode.equals("05") || keyCode.equals("06") || keyCode.equals("07") || keyCode.equals("08") || keyCode.equals("09"))
 		{
 			_gotoStr += String.format("%02d", Integer.parseInt(keyCode));
 			_gotoDigitCnt++;
 			if (_gotoDigitCnt == 4)
 			{
 				int gotostep = Integer.parseInt(_gotoStr);
 				if (gotostep <= MaxProgramSteps)
 				{
 					_pgmStep = gotostep;
 				}
 				_gotoDigitCnt = 0;
 				_keyCnt = 0;
 				_gotoStr = "";
 				autolift = true;
 				SetPendingFunction("");
 				isPendingOperand = false;
 			}
 		}
 	}	
 	
 	private void PrintGTOLabel(String name)
 	{
 		if (name.equals("ZERO"))
 		{
 				PrintByMode("GTO0");
 		}
 		else if (name.equals("ONE"))
 		{
 				PrintByMode("GTO1");
 		}
 		else if (name.equals("TWO"))
 		{
 				PrintByMode("GTO2");
 		}
 		else if (name.equals("THREE"))
 		{
 				PrintByMode("GTO3");
 		}
 		else if (name.equals("FOUR"))
 		{
 				PrintByMode("GTO4");
 		}
 		else if (name.equals("FIVE"))
 		{
 				PrintByMode("GTO5");
 		}
 		else if (name.equals("SIX"))
 		{
 				PrintByMode("GTO6");
 		}
 		else if (name.equals("SEVEN"))
 		{
 				PrintByMode("GTO7");
 		}
 		else if (name.equals("EIGHT"))
 		{
 				PrintByMode("GTO8");
 		}
 		else if (name.equals("NINE"))
 		{
 				PrintByMode("GTO9");
 		}
 		else if (name.equals("A"))
 		{
 				PrintByMode("GTOA");
 		}
 		else if (name.equals("B"))
 		{
 				PrintByMode("GTOB");
 		}
 		else if (name.equals("C"))
 		{
 				PrintByMode("GTOC");
 		}
 		else if (name.equals("D"))
 		{
 				PrintByMode("GTOD");
 		}
 		else if (name.equals("E"))
 		{
 				PrintByMode("GTOE");
 		}
 	}	
 
 	private void FindGTOProgramStep(String key1, String key2, String key3)
 	{
 		boolean labelFound = false;
 		int i;
 		for (i = _pgmStep; i <= MaxProgramSteps; i++)
 		{
 			if (_program.get(i).getKeyCode1().equals(key1) && _program.get(i).getKeyCode2().equals(key2) && _program.get(i).getKeyCode3().equals(key3))
 			{
 				// we have a match - set _pgmStep 
 				labelFound = true;
 				_branchExecuted = true;
 				_pgmStep = i;
 				break;
 			}
 		}
 		// if not found, search from start of program
 		if (labelFound == false)
 		{
 			for (i = 1; i < _pgmStep; i++)
 			{
 				if (_program.get(i).getKeyCode1().equals(key1) && _program.get(i).getKeyCode2().equals(key2) && _program.get(i).getKeyCode3().equals(key3))
 				{
 					// we have a match - set _pgmStep 
 					labelFound = true;
 					_branchExecuted = true;
 					_pgmStep = i;
 					break;
 				}
 			}
 		}
 		// If still not found - Error
 		if (labelFound == false)
 		{
 			isError = true;
 			PrintTrace();
 			autolift = true;
 			SetPendingFunction("");
 			isPendingOperand = false;
 		}
 		else
 		{
 			ClearError();
 			autolift = true;
 		}
 	}	
 	
 	private void ProcessGTOLabel(String name)
 	{
 		if (ValidLabelKey(name))
 		{
 			String key1;
 			String key2;
 			String key3;
 			int i;
 			// Build keycode search
 			if (_gotoFKeyLabel == true)
 			{
 				if (name.equals("A"))
 				{
 						PrintByMode("GTOa");
 				}
 				else if (name.equals("B"))
 				{
 						PrintByMode("GTOb");
 				}
 				else if (name.equals("C"))
 				{
 						PrintByMode("GTOc");
 				}
 				else if (name.equals("D"))
 				{
 						PrintByMode("GTOd");
 				}
 				else if (name.equals("E"))
 				{
 						PrintByMode("GTOe");
 				}
 				key1 = GetKeyCode(name);
 				key2 = "16";
 				key3 = "21";
 			}
 			else
 			{
 				PrintGTOLabel(name);
 				key1 = GetKeyCode(name);
 				key2 = "21";
 				key3 = "";
 				if (key1.equals("45"))
 				{
 					PrintByMode("GTO\u00a1");
 					double idx = Math.floor(GetI());
 					switch ((int)idx)
 					{
 						case 0:
 							key1 = "00";
 							break;
 						case 1:
 							key1 = "01";
 							break;
 						case 2:
 							key1 = "02";
 							break;
 						case 3:
 							key1 = "03";
 							break;
 						case 4:
 							key1 = "04";
 							break;
 						case 5:
 							key1 = "05";
 							break;
 						case 6:
 							key1 = "06";
 							break;
 						case 7:
 							key1 = "07";
 							break;
 						case 8:
 							key1 = "08";
 							break;
 						case 9:
 							key1 = "09";
 							break;
 						case 10:
 							key1 = "11";
 							break;
 						case 11:
 							key1 = "12";
 							break;
 						case 12:
 							key1 = "13";
 							break;
 						case 13:
 							key1 = "14";
 							break;
 						case 14:
 							key1 = "15";
 							break;
 						case 15:
 							key1 = "11";
 							key2 = "16";
 							key3 = "21";
 							break;
 						case 16:
 							key1 = "12";
 							key2 = "16";
 							key3 = "21";
 							break;
 						case 17:
 							key1 = "13";
 							key2 = "16";
 							key3 = "21";
 							break;
 						case 18:
 							key1 = "14";
 							key2 = "16";
 							key3 = "21";
 							break;
 						case 19:
 							key1 = "15";
 							key2 = "16";
 							key3 = "21";
 							break;
 						default:
 							if (idx < 0)
 							{
 								_pgmStep = (int)(Math.floor((_pgmStep + idx) / MaxProgramSteps) * -MaxProgramSteps + (_pgmStep + idx));
 								_branchExecuted = true;
 								autolift = true;
 								SetPendingFunction("");
 								isPendingOperand = false;
 								return;
 							}
 							else
 							{
 								// Invalid positive register number
 								isError = true;
 								PrintTrace();
 								autolift = true;
 								SetPendingFunction("");
 								isPendingOperand = false;
 								return;
 							}
 					}
 				}
 			}
 			// Search for key sequence in program
 			//Search to end of program from current location
 			FindGTOProgramStep(key1, key2, key3);
 		}
 		else
 		{
 			// Not a valid GTO label key
 			SetPendingFunction("");
 			isPendingOperand = false;
 			ProcessKeyStroke(name);
 		}
 	}	
 	
 	public String GetKeyCode(String name)
 	{
 		if (name.equals("A"))
 		{
 				return "11";
 		}
 		else if (name.equals("B"))
 		{
 				return "12";
 		}
 		else if (name.equals("C"))
 		{
 				return "13";
 		}
 		else if (name.equals("D"))
 		{
 				return "14";
 		}
 		else if (name.equals("E"))
 		{
 				return "15";
 		}
 		else if (name.equals("F"))
 		{
 				return "16";
 		}
 		else if (name.equals("LBL"))
 		{
 				return "21";
 		}
 		else if (name.equals("GTO"))
 		{
 				return "22";
 		}
 		else if (name.equals("GSB"))
 		{
 				return "23";
 		}
 		else if (name.equals("RTN"))
 		{
 				return "24";
 		}
 		else if (name.equals("BST"))
 		{
 				return "25";
 		}
 		else if (name.equals("SST"))
 		{
 				return "26";
 		}
 		else if (name.equals("YX"))
 		{
 				return "31";
 		}
 		else if (name.equals("LN"))
 		{
 				return "32";
 		}
 		else if (name.equals("EX"))
 		{
 				return "33";
 		}
 		else if (name.equals("TOPOLAR"))
 		{
 				return "34";
 		}
 		else if (name.equals("STO"))
 		{
 				return "35";
 		}
 		else if (name.equals("RCL"))
 		{
 				return "36";
 		}
 		else if (name.equals("SIN"))
 		{
 				return "41";
 		}
 		else if (name.equals("COS"))
 		{
 				return "42";
 		}
 		else if (name.equals("TAN"))
 		{
 				return "43";
 		}
 		else if (name.equals("TORECT"))
 		{
 				return "44";
 		}
 		else if (name.equals("IDX"))
 		{
 				return "45";
 		}
 		else if (name.equals("RCLI"))
 		{
 				return "46";
 		}
 		else if (name.equals("RS"))
 		{
 				return "51";
 		}
 		else if (name.equals("RECIP"))
 		{
 				return "52";
 		}
 		else if (name.equals("SQUARE"))
 		{
 				return "53";
 		}
 		else if (name.equals("SQR"))
 		{
 				return "54";
 		}
 		else if (name.equals("PERC"))
 		{
 				return "55";
 		}
 		else if (name.equals("SUMPLUS"))
 		{
 				return "56";
 		}
 		else if (name.equals("PRINT"))
 		{
 				return "-14";
 		}
 		else if (name.equals("ENTER"))
 		{
 				return "-21";
 		}
 		else if (name.equals("PLUS"))
 		{
 				return "-55";
 		}
 		else if (name.equals("RDOWN"))
 		{
 				return "-31";
 		}
 		else if (name.equals("XY"))
 		{
 				return "-41";
 		}
 		else if (name.equals("CLX"))
 		{
 				return "-51";
 		}
 		else if (name.equals("FIX"))
 		{
 				return "-11";
 		}
 		else if (name.equals("SCI"))
 		{
 				return "-12";
 		}
 		else if (name.equals("ENG"))
 		{
 				return "-13";
 		}
 		else if (name.equals("CHS"))
 		{
 				return "-22";
 		}
 		else if (name.equals("EEX"))
 		{
 				return "-23";
 		}
 		else if (name.equals("DIV"))
 		{
 				return "-24";
 		}
 		else if (name.equals("SUB"))
 		{
 				return "-45";
 		}
 		else if (name.equals("MULT"))
 		{
 				return "-35";
 		}
 		else if (name.equals("NINE"))
 		{
 				return "09";
 		}
 		else if (name.equals("ZERO"))
 		{
 				return "00";
 		}
 		else if (name.equals("EIGHT"))
 		{
 				return "08";
 		}
 		else if (name.equals("SEVEN"))
 		{
 				return "07";
 		}
 		else if (name.equals("FOUR"))
 		{
 				return "04";
 		}
 		else if (name.equals("FIVE"))
 		{
 				return "05";
 		}
 		else if (name.equals("SIX"))
 		{
 				return "06";
 		}
 		else if (name.equals("ONE"))
 		{
 				return "01";
 		}
 		else if (name.equals("TWO"))
 		{
 				return "02";
 		}
 		else if (name.equals("THREE"))
 		{
 				return "03";
 		}
 		else if (name.equals("DEC"))
 		{
 				return "-62";
 		}
 		else if (name.equals("DSP"))
 		{
 				return "-63";
 		}
 		return "";
 	}	
 
 	public String DisplayProgram(int offset) {
 
 		int i = _pgmStep + offset;
 		String line = "";
 		if (i < 0) {
 			i = MaxProgramSteps;
 		}
 		if (i == 0) {
 			return " 000";
 		} else {
 			line = String.format(" %-6s%2s%3s%3s",
 					String.format("%03d", i),
 					_program.get(i).getKeyCode3(),
 					_program.get(i).getKeyCode2(), 
 					_program.get(i).getKeyCode1());
 		}
 		return line;
 	}
 
 	@Override
 	public String GetDisplay() {
 		if (GetRunMode() == false) {
 			return DisplayProgram(0);
 		}
 
 		if (_isRandomDisplay == true) {
 			return _randomDisplay;
 		}
 
 		String result = "";
 
 		// If decimal is first digit entered,
 		// display it and return here - do not
 		// try to parse as number yet.
 		if (isLeadingDecimal) {
 			return " .";
 		}
 
 		if (isError) {
 			return "Error";
 		}
 
 		if (isEnteringData) {
 			if (isEnteringExponent) {
 				String leading = (GetX() >= 0) ? " " : "";
 				if (!_inputDigits.contains(".")) {
 					_inputDigits += ".";
 				}
 				result = String.format("%-12s%s%s", String.format(
 						"%s%s", leading, _inputDigits), _exponentSign,
 						_exponentDigits);
 			} else {
 				// HP97 adds an ending period as a "cursor"
 				// to the end of entered digits. If a decimal
 				// has already been entered, the ending decimal
 				// no longer appears.
 				if (_inputDigits.contains(".")) {
 					result = _inputDigits;
 				} else {
 					result = _inputDigits + ".";
 				}
 				if (!result.startsWith("-")) {
 					result = " " + result;
 				}
 			}
 		} else {
 			if (isDisplayOverflow() && _displayMode == displayModes.Fixed) {
 				String raw = String.format("%.10E", GetX());
 				int idxE = raw.indexOf('E');
 				String basestr = raw.substring(0, idxE - 1);
 				String expstr = raw.substring(idxE + 2);
 				double exponent = Double.parseDouble(String.format(raw
 						.substring(idxE + 1)));
 				char signchar = (exponent < 0) ? '-' : ' ';
 				if (_displayDigitCount == 0) {
 					if (!basestr.contains(".")) {
 						basestr = basestr + ".";
 					}
 				}
 				if (!basestr.startsWith("-")) {
 					basestr = " " + basestr;
 				}
 				result = String.format("%-12s%s%s", basestr, signchar, expstr);
 				if (decimalOn) {
 					return result.replace(",", ".");
 				} else {
 					return result.replace(",", ".").replace(".", " ");
 				}
 			} else if (_displayMode == displayModes.Fixed) {
 				// build fixed format
 				result = GetFixedDisplay();
 			} else if (_displayMode == displayModes.Scientific) {
 				result = GetScientificDisplay();
 			} else if (_displayMode == displayModes.Engineering) {
 				result = GetEngineeringDisplay();
 			}
 		}
 		if (decimalOn) {
 			return result;
 		} else {
 			return result.replace(".", " ");
 		}
 	}
 
 	public int GetProgramLenth() {
 		int i;
 		for (i = MaxProgramSteps; i > 0; i--) {
 			// On first non R/S instruction detected counting back from
 			// maxsteps...
 			if (!_program.get(i).getKeyCode1().equals("51")) {
 				break;
 			}
 		}
 		return i;
 	}
 
 	public void InsertInstruction(String lbl, String k1, String k2, String k3) {
 		_keyCnt = -1;
 		_pgmStep += 1;
 
 		if (_pgmStep > MaxProgramSteps) {
 			_pgmStep--;
 			return;
 		}
 
 		// Shift instructions
 		for (int i = MaxProgramSteps; i > _pgmStep; i--) {
 			_program.set(i, _program.get(i - 1));
 		}
 
 		// insert new instruction
 		_program.set(_pgmStep, new PgmInstruction(lbl));
 		_program.get(_pgmStep).setKeyCode1(k1);
 		_program.get(_pgmStep).setKeyCode2(k2);
 		_program.get(_pgmStep).setKeyCode3(k3);
 	}
 
 	public HP97Program GetProgram() {
 		HP97Program pgm = new HP97Program();
 		pgm.AngleMode = _angleMode;
 		pgm.DisplayMode = _displayMode;
 		pgm.DisplaySize = (int) _displayDigitCount;
 		pgm.IsProgram = true;
 		pgm.Program = new ArrayList<PgmInstruction>(_program);
 		pgm.Flags = new ArrayList<Boolean>(_flag);
 		return pgm;
 	}
 
 	public HP97Program GetData() {
 		HP97Program pgm = new HP97Program();
 		pgm.IsProgram = false;
 		pgm.Registers = new ArrayList<Double>(_register);
 		return pgm;
 	}
 
 	private String RenderLine(String data) {
 		// We're assuming a 22 character line here for the "HP97"
 		String line = String.format("%22s", data);
 		PrintQueue.offer(line);
 		return line;
 	}
 
 	public void Print() {
 		try {
 			if (GetHP67PrintMode() == true) {
 				// There will be a value in X to display
 				for (int i = 0; i < 10; i++) {
 					if (decimalOn == false) {
 						decimalOn = true;
 					} else {
 						decimalOn = false;
 					}
 					DisplayUpdate();
 					Thread.sleep(500); // half second sleep
 				}
 				decimalOn = true;
 			} else {
 				// if (OnPrint != null)
 				// {
 				// OnPrint(this, new PrintEventArgs());
 				// }
 			}
 		} catch (Exception ex) {
 
 		}
 	}
 
 	public static String padRight(String s, int n, char c) {
 		StringBuilder sb = new StringBuilder(s);
 		for (int i = 0; i < n; i++) {
 			sb.append(c);
 		}
 		return sb.toString();
 	}
 
 	public boolean isDisplayOverflow() {
 		double a = Math.abs(GetX());
 
 		if (a == 0) {
 			return false;
 		}
 
 		// Sets X to limit of min or max value
 		if (isOverFlowValue(a)) {
 			SetX((GetX() < 0) ? -9.999999999e+99 : 9.999999999e+99);
 			a = Math.abs(GetX());
 		}
 
 		// Decide if 'a' has more significant digits than current
 		// display size allows.
 		double iplength = 0;
 		if (a >= 1) {
 			iplength = Math.floor(Math.log10(Math.floor(a))) + 1;
 		}
 
 		if (iplength > 10) {
 			return true;
 		}
 
 		if (a < 1) {
 			// if fractional part rounds to zero at DSP setting
 			// then switch to SCI.
 			double aRoundedToDSP = Math.round((a - Math2.Truncate(a))
 					* Math.pow(10, (int) _displayDigitCount))
 					/ Math.pow(10, (int) _displayDigitCount);
 			if (aRoundedToDSP == 0) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public String FormatPrintLine() {
 		String line = "";
 		StringBuilder zeros = new StringBuilder();
 		for (int i = 0; i < _displayDigitCount; i++) {
 			zeros.append("0");
 		}
 		// Format depending on print mode
 		if (isEnteringData == true) {
 			String tmp;
 			// This little snippet emulates an hp97 behaviour.
 			// If an entered digit sequence is printed before any other
 			// function key, the literal number is printed - except if the
 			// number is an integer - in which case HP97 adds .00 to the end -
 			// when in fixed mode - but doesn't when in SCI or ENG mode...
 			if (isEnteringExponent == true) {
 				tmp = _inputDigits + ((_exponentSign == ' ') ? "+" : "-")
 						+ _exponentDigits;
 			} else {
 				if (!_inputDigits.contains(".")) {
 					if (_displayMode == displayModes.Fixed) {
 						tmp = _inputDigits + "." + zeros.toString();
 					} else {
 						tmp = _inputDigits + ".";
 					}
 				} else {
 					if (isLeadingDecimal) {
 						// This prints the same display
 						// as the HP97 when the user enters a single
 						// decimal, then hits PRINTX button.
 						if (_displayMode == displayModes.Fixed) {
 							_inputDigits = "." + zeros.toString();
 						} else {
 							_inputDigits = ".";
 						}
 						isLeadingDecimal = false;
 					}
 					if (_displayMode == displayModes.Fixed) {
 						if (_inputDigits.contains(".")) {
 							int decptr = _inputDigits.indexOf(".");
 							String frac = _inputDigits.substring(decptr);
 							if (frac.length() - 1 < _displayDigitCount) {
 								tmp = padRight(_inputDigits,
 										(int) (_inputDigits.length()
 												+ _displayDigitCount
 												- frac.length() + 1), '0');
 							} else {
 								tmp = _inputDigits;
 							}
 						} else {
 							tmp = _inputDigits = "." + zeros.toString();
 						}
 					} else {
 						tmp = _inputDigits;
 					}
 				}
 			}
 			line = String.format("%s%s", tmp, String.format("%5s", ""));
 		} else {
 			if (isDisplayOverflow()) {
 				String raw = String.format("%.10E", GetX()).replace("E", "");
 				line = String.format("%s%s", raw, String.format("%5s",
 						"***"));
 			} else {
 				if (_displayMode == displayModes.Fixed) {
 					line = String.format("%s%s", GetDisplay(), String
 							.format("%5s", "***"));
 				} else {
 					if (_displayMode == displayModes.Scientific) {
 						// Basically removing the 'E' from the standard C#
 						// format
 						String raw = String.format("%" +
 								"E", GetX()).replace(
 								"E", "");
 						line = String.format("%s%s", raw, String.format(
 								"%1$5s", "***"));
 					} else {
 						if (_displayMode == displayModes.Engineering) {
 							String raw = GetDisplay();
 							String suffix = raw.substring(raw.length() - 3, raw
 									.length() - 3 + 3);
 							suffix = suffix.replace(" ", "+");
 							String prefix = raw.substring(0, raw.length() - 3)
 									.trim();
 							raw = prefix + suffix;
 							line = String.format("%s%s", raw, String
 									.format("%5s", "***"));
 						}
 					}
 				}
 			}
 		}
 		return line;
 	}
 
 	public void PrintByMode(String label) {
 		if (GetRunning()) {
 			return;
 		}
 		String digits;
 		String line;
 
 		if (GetPrintMode() == printModes.Manual) {
 			return;
 		}
 
 		digits = FormatPrintLine();
 
 		if (isEnteringData) {
 			digits = digits.trim();
 			line = String.format("%s%s", digits, String.format("%5s",
 					label));
 			RenderLine(line);
 		} else {
 			line = String.format("%s", String.format("%5s", label));
 			RenderLine(line);
 		}
 		Print();
 	}
 
 	public void SetFlag(int idx, Boolean value) {
 		if (value)
 			PrintByMode("SF" + idx);
 		else
 			PrintByMode("CF" + idx);
 		if (idx > 3 || idx < 0)
 			return;
 		_flag.set(idx, value);
 	}
 
 	public void SetData(HP97Program pgm) {
 		for (int i = 0; i < MaxRegisters; i++) {
 			_register.set(i, pgm.Registers.get(i));
 		}
 		SetFlag(3, true);
 	}
 
 	public void SetProgram(HP97Program pgm) {
 		_angleMode = pgm.AngleMode;
 		_displayMode = pgm.DisplayMode;
 		_displayDigitCount = pgm.DisplaySize;
 		_program = new ArrayList<PgmInstruction>(pgm.Program);
 		_flag = new ArrayList<Boolean>(pgm.Flags);
 		BuildDisplayFormats();
 		DisplayUpdate();
 	}
 
 	public void DisplayUpdate() {
 		if (_displayHandler != null) {
 			_displayHandler
 					.DisplayUpdateHandler(this, new DisplayEventArgs(""));
 		}
 	}
 
 	private void BuildDisplayFormats() {
 		String digits = String.format("%d", (int) _displayDigitCount);
 		_displayFixedFormat = "%." + digits + "f";
 		_displaySciFormat = "%." + digits + "E";
 		_displayEngFormat = "%." + digits + "E";
 		
 	}
 
 	public void MergeProgram(HP97Program pgm) {
 		// load pgm.Program starting at curr step + 1
 		// We only restore program steps - no flags, trig modes, etc.
 		int j = 1;
 		for (int i = _pgmStep + 1; i <= MaxProgramSteps; i++) {
 			_program.set(i, pgm.Program.get(j++));
 		}
 	}
 
 	public Boolean GetFlag(int idx) {
 		if (idx > 3 || idx < 0)
 			return false;
 		return _flag.get(idx);
 	}
 
 	public void SetDataEntryFlag() {
 		_flag.set(3, true);
 	}
 
 	private Boolean _runMode;
 
 	public void SetRunMode(Boolean value) {
 		ClearError();
 		_runMode = value;
 	}
 
 	public Boolean GetRunMode() {
 		return _runMode;
 	}
 
 	public void ClearError() {
 		isError = false;
 		SetPendingFunction("");
 		isPendingOperand = false;
 	}
 
 	protected void SetI(double value) {
 		_register.set(25, value);
 	}
 
 	public double GetI() {
 		return _register.get(25);
 	}
 
 	private void initRegisters() {
 		for (int i = 0; i < this.MaxRegisters; i++) {
 			_register.add(0.00);
 		}
 	}
 
 	public HP97() {
 		super();
 		SetPowerOn(true);
 		_displayMode = displayModes.Fixed;
 		_displayFixedFormat = "%.2f";
 		_displaySciFormat = "%.2E";
 		_displayEngFormat = "%.2E";
 		_displayDigitCount = 2;
 		_pendingRegMath = "";
 		_angleMode = angleModes.Degrees;
 		_runMode = false;
 		SetPrintMode(printModes.Manual);
 		PrintQueue = new java.util.LinkedList<String>();
 		NextStep = new java.util.Stack<Integer>();
 		for (int i = 0; i < MaxFlags; i++) {
 			_flag.add(false);
 		}
 		initRegisters();
 		ClearProgram();
 		SetRunMode(true);
 		SetRunning(false);
 	}
 
 	public void ClearProgram() {
 		if (GetRunMode()) {
 			return;
 		}
 		_program.clear();
 		// Clear Flags
 		for (int i = 0; i < 4; i++) {
 			_flag.set(i, false);
 		}
 		_angleMode = angleModes.Degrees;
 		// Clear Program
 		_pgmStep = 0;
 		_keyCnt = 0;
 		_program.add(new PgmInstruction("000"));
 		// _program[0] = new PgmInstruction("000");
 		for (int i = 1; i <= MaxProgramSteps; i++) {
 			PgmInstruction p = new PgmInstruction("R/S");
 			p.setKeyCode1("51");
 			_program.add(p);
 			// _program[i] = p;
 		}
 		if (_displayHandler != null) {
 			_displayHandler.DisplayUpdateHandler(this, new DisplayEventArgs(
 					"Clear"));
 		}
 
 		autolift = false;
 	}
 
 	public double GetRegister(int idx) {
 		if (idx >= 0 && idx < MaxRegisters) {
 			return _register.get(idx);
 		} else {
 			isError = true;
 			return 0;
 		}
 	}
 
 	public double GetRegister(String name) {
 		double tmp = 0;
 		if (name.equals("ZERO")) {
 			tmp = _register.get(0);
 		} else if (name.equals("ONE")) {
 			tmp = _register.get(1);
 		} else if (name.equals("TWO")) {
 			tmp = _register.get(2);
 		} else if (name.equals("THREE")) {
 			tmp = _register.get(3);
 		} else if (name.equals("FOUR")) {
 			tmp = _register.get(4);
 		} else if (name.equals("FIVE")) {
 			tmp = _register.get(5);
 		} else if (name.equals("SIX")) {
 			tmp = _register.get(6);
 		} else if (name.equals("SEVEN")) {
 			tmp = _register.get(7);
 		} else if (name.equals("EIGHT")) {
 			tmp = _register.get(8);
 		} else if (name.equals("NINE")) {
 			tmp = _register.get(9);
 		} else if (name.equals("A")) {
 			tmp = _register.get(20);
 		} else if (name.equals("B")) {
 			tmp = _register.get(21);
 		} else if (name.equals("C")) {
 			tmp = _register.get(22);
 		} else if (name.equals("D")) {
 			tmp = _register.get(23);
 		} else if (name.equals("E")) {
 			tmp = _register.get(24);
 		} else if (name.equals("RCLI")) {
 			tmp = GetI();
 		} else if (name.equals("IDX")) {
 			if (Math.floor(Math.abs(GetI())) < MaxRegisters) {
 				tmp = _register.get((int) Math.floor(Math.abs(GetI())));
 			} else {
 				// Error - index of of bounds
 				isError = true;
 			}
 		}
 		return tmp;
 	}
 
 	private double AssignRegValue(int i, double value) {
 		double retval;
 		if (_pendingRegMath.length() > 0) {
 			if (_pendingRegMath.equals("MULT")) {
 				retval = _register.get(i) * value;
 			} else if (_pendingRegMath.equals("SUB")) {
 				retval = _register.get(i) - value;
 			} else if (_pendingRegMath.equals("PLUS")) {
 				retval = _register.get(i) + value;
 			} else if (_pendingRegMath.equals("DIV")) {
 				retval = _register.get(i) / value;
 			} else {
 				retval = value;
 			}
 		} else {
 			retval = value;
 		}
 
 		if (isOverFlowValue(retval)) {
 			isError = true;
 			retval = _register.get(i);
 		}
 
 		return retval;
 	}
 
 	public static final boolean isOverFlowValue(Double v) {
 		Double a = Math.abs(v);
 
 		if (a == 0)
 			return false;
 
 		if (a >= 9.999999999e+99) {
 			return true;
 		}
 
 		if (a <= 9.999999999e-99) {
 			return true;
 		}
 
 		return false;
 	}
 
 	public void SetRegister(String name, double value) {
 		if (name.equals("ZERO")) {
 			_register.set(0, AssignRegValue(0, value));
 		} else if (name.equals("ONE")) {
 			_register.set(1, AssignRegValue(1, value));
 		} else if (name.equals("TWO")) {
 			_register.set(2, AssignRegValue(2, value));
 		} else if (name.equals("THREE")) {
 			_register.set(3, AssignRegValue(3, value));
 		} else if (name.equals("FOUR")) {
 			_register.set(4, AssignRegValue(4, value));
 		} else if (name.equals("FIVE")) {
 			_register.set(5, AssignRegValue(5, value));
 		} else if (name.equals("SIX")) {
 			_register.set(6, AssignRegValue(6, value));
 		} else if (name.equals("SEVEN")) {
 			_register.set(7, AssignRegValue(7, value));
 		} else if (name.equals("EIGHT")) {
 			_register.set(8, AssignRegValue(8, value));
 		} else if (name.equals("NINE")) {
 			_register.set(9, AssignRegValue(9, value));
 		} else if (name.equals("A")) {
 			_register.set(20, AssignRegValue(20, value));
 		} else if (name.equals("B")) {
 			_register.set(21, AssignRegValue(21, value));
 		} else if (name.equals("C")) {
 			_register.set(22, AssignRegValue(22, value));
 		} else if (name.equals("D")) {
 			_register.set(23, AssignRegValue(23, value));
 		} else if (name.equals("E")) {
 			_register.set(24, AssignRegValue(24, value));
 		} else if (name.equals("RCLI")) {
 			SetI(AssignRegValue(25, value));
 		} else if (name.equals("IDX")) {
 			if (Math.floor(Math.abs(GetI())) < MaxRegisters) {
 				_register.set((int) Math.floor(Math.abs(GetI())),
 						AssignRegValue((int) Math.floor(Math.abs(GetI())),
 								value));
 			} else {
 				// Error - index of of bounds
 				isError = true;
 			}
 		}
 	}
 
 	public void SetIsFShift(boolean value) {
 		isFShift = value;
 	}
 
 	public boolean GetIsFShift() {
 		return isFShift;
 	}
 
 	private boolean ValidRegisterKey(String name) {
 		boolean isValid = false;
 		if (name.equals("PLUS") || name.equals("MULT") || name.equals("DIV")
 				|| name.equals("SUB") || name.equals("ZERO")
 				|| name.equals("ONE") || name.equals("TWO")
 				|| name.equals("THREE") || name.equals("FOUR")
 				|| name.equals("FIVE") || name.equals("SIX")
 				|| name.equals("SEVEN") || name.equals("EIGHT")
 				|| name.equals("NINE") || name.equals("A") || name.equals("B")
 				|| name.equals("C") || name.equals("D") || name.equals("E")
 				|| name.equals("RCLI") || name.equals("SUMPLUS")
 				|| name.equals("IDX")) {
 			isValid = true;
 		} else {
 			isValid = false;
 		}
 		return isValid;
 	}
 
 	private boolean ValidFlagKey(String name) {
 		boolean isValid = false;
 		if (name.equals("ZERO") || name.equals("ONE") || name.equals("TWO")
 				|| name.equals("THREE")) {
 			isValid = true;
 		} else {
 			isValid = false;
 		}
 		return isValid;
 	}
 
 	public static final boolean ValidNumberKey(String name) {
 		boolean isValid = false;
 		if (name.equals("ZERO") || name.equals("ONE") || name.equals("TWO")
 				|| name.equals("THREE") || name.equals("FOUR")
 				|| name.equals("FIVE") || name.equals("SIX")
 				|| name.equals("SEVEN") || name.equals("EIGHT")
 				|| name.equals("NINE") || name.equals("IDX")) {
 			isValid = true;
 		} else {
 			isValid = false;
 		}
 		return isValid;
 	}
 
 	public static final boolean ValidLabelKey(String name) {
 		boolean isValid = false;
 		if (name.equals("ZERO") || name.equals("ONE") || name.equals("TWO")
 				|| name.equals("THREE") || name.equals("FOUR")
 				|| name.equals("FIVE") || name.equals("SIX")
 				|| name.equals("SEVEN") || name.equals("EIGHT")
 				|| name.equals("NINE") || name.equals("IDX")
 				|| name.equals("A") || name.equals("B") || name.equals("C")
 				|| name.equals("D") || name.equals("E")) {
 			isValid = true;
 		} else {
 			isValid = false;
 		}
 		return isValid;
 	}
 
 	public void PrintTrace() {
 		if (isError && !(GetPrintMode() == printModes.Manual)) {
 			RenderLine("ERROR");
 			Print();
 			return;
 		}
 
 		if (GetPrintMode() == printModes.Trace) {
 			isEnteringData = false;
 			RenderLine(FormatPrintLine());
 			Print();
 		}
 	}
 
 	public void SetDisplayMode(String name) {
 		PrintByMode(name);
 		if (name.equals("FIX")) {
 			_displayMode = displayModes.Fixed;
 		} else if (name.equals("SCI")) {
 			_displayMode = displayModes.Scientific;
 		} else if (name.equals("ENG")) {
 			_displayMode = displayModes.Engineering;
 		}
 		BuildDisplayFormats();
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void SetSignificantDigitCount(String name) {
 		if (name.equals("ZERO")) {
 			PrintByMode("DSP0");
 			_displayDigitCount = 0;
 		} else if (name.equals("ONE")) {
 			PrintByMode("DSP1");
 			_displayDigitCount = 1;
 		} else if (name.equals("TWO")) {
 			PrintByMode("DSP2");
 			_displayDigitCount = 2;
 		} else if (name.equals("THREE")) {
 			PrintByMode("DSP3");
 			_displayDigitCount = 3;
 		} else if (name.equals("FOUR")) {
 			PrintByMode("DSP4");
 			_displayDigitCount = 4;
 		} else if (name.equals("FIVE")) {
 			PrintByMode("DSP5");
 			_displayDigitCount = 5;
 		} else if (name.equals("SIX")) {
 			PrintByMode("DSP6");
 			_displayDigitCount = 6;
 		} else if (name.equals("SEVEN")) {
 			PrintByMode("DSP7");
 			_displayDigitCount = 7;
 		} else if (name.equals("EIGHT")) {
 			PrintByMode("DSP8");
 			_displayDigitCount = 8;
 		} else if (name.equals("NINE")) {
 			PrintByMode("DSP9");
 			_displayDigitCount = 9;
 		} else if (name.equals("IDX")) {
 			PrintByMode("DSP\u00a1");
 			if (Math.floor(Math.abs(GetI())) > 9) {
 				isError = true;
 			} else {
 				_displayDigitCount = Math.floor(Math.abs(GetI()));
 			}
 		}
 		BuildDisplayFormats();
 		PrintTrace();
 	}
 	
 	public String GetEngineeringDisplay() {
 		String result;
 		String basestr;
 		String expstr;
 		String expformat = "%02d";
 		String raw = String.format(_displayEngFormat, GetX());
 
 		// Split into basenum and exponent values
 		int idxE = raw.indexOf('E');
 		double basenum;
 		if (_displayDigitCount == 9) {
 			basenum = Double.parseDouble(raw.substring(0, idxE - 1));
 		} else {
 			basenum = Double.parseDouble(raw.substring(0, idxE));
 		}
 
 		double exponent = Double.parseDouble(String.format(raw
 				.substring(idxE + 1)));
 
 		// Adjust exponent to nearest multiple of three
 		// and adjust basenum accordingly
 		double near3exp = Math.floor(exponent / 3) * 3;
 		double powadj = Math.pow(10, near3exp - exponent);
 		basenum = basenum / powadj;
 		exponent = near3exp;
 
 		// Build basenum format String:
 		// significant digits on left of decimal place (account for possible '-'
 		// sign)
 		double significant = Math2.SignificantDigitsToLeft(basenum);
 		double digitsright = _displayDigitCount - (significant - 1);
 		String sDigits = String.format("%d", (int) digitsright);
 		String sFormat = "%" + "." + sDigits + "f";
 		// build format on left of decimal
 
 		// Format Display result
 		basestr = String.format(sFormat, basenum);
 		expstr = String.format(expformat, (int)Math.abs(exponent));
 		char signchar = (exponent < 0) ? '-' : ' ';
 		if (!basestr.contains(".")) {
 			basestr += ".";
 		}
 		if (!basestr.startsWith("-")) {
 			basestr = " " + basestr;
 		}
 		result = String.format("%-12s%s%s", basestr, signchar, expstr);
 		return result;
 	}
 
 	public String GetScientificDisplay() {
 		String result;
 		String raw = String.format(_displaySciFormat, GetX());
 		int idxE = raw.indexOf('E');
 		String basestr;
 		if (_displayDigitCount == 9) {
 			basestr = raw.substring(0, idxE - 1);
 		} else {
 			basestr = raw.substring(0, idxE);
 		}
 		String expstr = raw.substring(idxE + 2);
 		double exponent = Double.parseDouble(String.format(raw
 				.substring(idxE + 1)));
 		char signchar = (exponent < 0) ? '-' : ' ';
 		if (_displayDigitCount == 0) {
 			if (!basestr.contains(".")) {
 				basestr = basestr + ".";
 			}
 		}
 		if (!basestr.startsWith("-")) {
 			basestr = " " + basestr;
 		}
 		result = String.format("%-12s%s%s", basestr, signchar, expstr);
 		return result;
 	}
 
 	public String GetFixedDisplay() {
 		String result;
 		// if (intpart == 0)
 		int digitstoleft = Math2.SignificantDigitsToLeft(GetX());
 		_displayFixedFormat = "%."+String.format("%d", (int) _displayDigitCount)+"f";
 		String line = String.format(_displayFixedFormat, GetX());
 
 		if (_displayDigitCount == 0 || 10 - digitstoleft == 0) {
 			if (!line.contains(".")) {
 				line = line + ".";
 			}
 		}
 		if (!line.startsWith("-")) {
 			line = " " + line;
 		}
 		result = line;
 		return result;
 	}
 
 	public void GoSub(String label)
 	{
 		GoSubLabel(label);
		
		if (_gotoFKeyLabel==true)
			return;
		
 		// If Gosub resolves a target label, then
 		// the stack will have at least 1 return entry
 		if (NextStep.size() > 0)
 		{
			if (GetRunning()==true)
 			{
				RunSubRoutine();
			}
			else
			{
				SetRunning(true);
				Thread thr = new Thread()
 				{
					public void run()
					{
						RunSubRoutine();
					}
				};
				thr.start();
			}
 		}
 	}
 	
 	public void GoSubLabel(String name)
 	{
 		if (name.length() == 0)
 		{
 			SetPendingFunction("GSB");
 			isPendingOperand = true;
 			_gotoFKeyLabel = false;
 		}
 		else
 		{
 			if (name.equals("F"))
 			{
 				_gotoFKeyLabel = true;
 				return;
 			}
 			else
 			{
 				ProcessGSBLabel(name);
				_gotoFKeyLabel = false;
 			}
 		}
 	}
 
 	private void ProcessGSBLabel(String name)
 	{
 		if (ValidLabelKey(name))
 		{
 			String key1;
 			String key2;
 			String key3;
 			int i;
 			// Build keycode search
 			if (_gotoFKeyLabel == true)
 			{
 				if (name.equals("A"))
 				{
 						PrintByMode("GSBa");
 				}
 				else if (name.equals("B"))
 				{
 						PrintByMode("GSBb");
 				}
 				else if (name.equals("C"))
 				{
 						PrintByMode("GSBc");
 				}
 				else if (name.equals("D"))
 				{
 						PrintByMode("GSBd");
 				}
 				else if (name.equals("E"))
 				{
 						PrintByMode("GSBe");
 				}
 				key1 = GetKeyCode(name);
 				key2 = "16";
 				key3 = "21";
 			}
 			else
 			{
 				PrintGSBLabelKey(name);
 				key1 = GetKeyCode(name);
 				key2 = "21";
 				key3 = "";
 				if (key1.equals("45"))
 				{
 					PrintByMode("GSB\u00a1");
 					double idx = Math.floor(GetI());
 					switch ((int)idx)
 					{
 						case 0:
 							key1 = "00";
 							break;
 						case 1:
 							key1 = "01";
 							break;
 						case 2:
 							key1 = "02";
 							break;
 						case 3:
 							key1 = "03";
 							break;
 						case 4:
 							key1 = "04";
 							break;
 						case 5:
 							key1 = "05";
 							break;
 						case 6:
 							key1 = "06";
 							break;
 						case 7:
 							key1 = "07";
 							break;
 						case 8:
 							key1 = "08";
 							break;
 						case 9:
 							key1 = "09";
 							break;
 						case 10:
 							key1 = "11";
 							break;
 						case 11:
 							key1 = "12";
 							break;
 						case 12:
 							key1 = "13";
 							break;
 						case 13:
 							key1 = "14";
 							break;
 						case 14:
 							key1 = "15";
 							break;
 						case 15:
 							key1 = "11";
 							key2 = "16";
 							key3 = "21";
 							break;
 						case 16:
 							key1 = "12";
 							key2 = "16";
 							key3 = "21";
 							break;
 						case 17:
 							key1 = "13";
 							key2 = "16";
 							key3 = "21";
 							break;
 						case 18:
 							key1 = "14";
 							key2 = "16";
 							key3 = "21";
 							break;
 						case 19:
 							key1 = "15";
 							key2 = "16";
 							key3 = "21";
 							break;
 						default:
 							if (idx < 0)
 							{
 								PushNextStep(_pgmStep);
 								_branchExecuted = true;
 								_pgmStep = (int)(Math.floor((_pgmStep + idx) / MaxProgramSteps) * -MaxProgramSteps + (_pgmStep + idx));
 								autolift = true;
 								SetPendingFunction("");
 								isPendingOperand = false;
 								return;
 							}
 							else
 							{
 								// Invalid positive register number
 								isError = true;
 								autolift = true;
 								SetPendingFunction("");
 								isPendingOperand = false;
 								return;
 							}
 					}
 				}
 			}
 			// Search for key sequence in program
 			//Search to end of program from current location
 			FindGSBProgramStep(key1, key2, key3);
 		}
 		else
 		{
 			// Not a valid GTO label key
 			SetPendingFunction("");
 			isPendingOperand = false;
 			ProcessKeyStroke(name);
 		}
 	}
 	
 	private void FindGSBProgramStep(String key1, String key2, String key3)
 	{
 		boolean labelFound = false;
 		int i;
 		for (i = _pgmStep; i <= MaxProgramSteps; i++)
 		{
 			if (_program.get(i).getKeyCode1().equals(key1) && _program.get(i).getKeyCode2().equals(key2) && _program.get(i).getKeyCode3().equals(key3))
 			{
 				// we have a match - set _pgmStep 
 				labelFound = true;
 				PushNextStep(_pgmStep);
 				_branchExecuted = true;
 				_pgmStep = i;
 				break;
 			}
 		}
 		// if not found, search from start of program
 		if (labelFound == false)
 		{
 			for (i = 1; i < _pgmStep; i++)
 			{
 				if (_program.get(i).getKeyCode1().equals(key1) && _program.get(i).getKeyCode2().equals(key2) && _program.get(i).getKeyCode3().equals(key3))
 				{
 					// we have a match - set _pgmStep 
 					labelFound = true;
 					PushNextStep(_pgmStep);
 					_branchExecuted = true;
 					_pgmStep = i;
 					break;
 				}
 			}
 		}
 		// If still not found - Error
 		if (labelFound == false)
 		{
 			isError = true;
 			autolift = true;
 			SetPendingFunction("");
 			isPendingOperand = false;
 		}
 		else
 		{
 			ClearError();
 			autolift = true;
 		}
 	}
 	
 	private void PrintGSBLabelKey(String name)
 	{
 		if (name.equals("ZERO"))
 		{
 				PrintByMode("GSB0");
 		}
 		else if (name.equals("ONE"))
 		{
 				PrintByMode("GSB1");
 		}
 		else if (name.equals("TWO"))
 		{
 				PrintByMode("GSB2");
 		}
 		else if (name.equals("THREE"))
 		{
 				PrintByMode("GSB3");
 		}
 		else if (name.equals("FOUR"))
 		{
 				PrintByMode("GSB4");
 		}
 		else if (name.equals("FIVE"))
 		{
 				PrintByMode("GSB5");
 		}
 		else if (name.equals("SIX"))
 		{
 				PrintByMode("GSB6");
 		}
 		else if (name.equals("SEVEN"))
 		{
 				PrintByMode("GSB7");
 		}
 		else if (name.equals("EIGHT"))
 		{
 				PrintByMode("GSB8");
 		}
 		else if (name.equals("NINE"))
 		{
 				PrintByMode("GSB9");
 		}
 		else if (name.equals("A"))
 		{
 				PrintByMode("GSBA");
 		}
 		else if (name.equals("B"))
 		{
 				PrintByMode("GSBB");
 		}
 		else if (name.equals("C"))
 		{
 				PrintByMode("GSBC");
 		}
 		else if (name.equals("D"))
 		{
 				PrintByMode("GSBD");
 		}
 		else if (name.equals("E"))
 		{
 				PrintByMode("GSBE");
 		}
 	}
 
 	public void PushNextStep(int pgmStep)
 	{
 		NextStep.push(pgmStep);
 		_highestStackCount = NextStep.size();
 	}
 
 	public int PopNextStep()
 	{
 		return NextStep.pop();
 	}
 
 	public void CheckFlag(int flagno)
 	{
 		// We'll enter this function when Shift GSB is selected (flag -1)
 		// And later when flag number is chosen (0-3).
 		if (flagno == -1)
 		{
 			SetPendingFunction("F?");
 			isPendingOperand = true;
 		}
 		else
 		{
 			PrintByMode("F" + flagno + "?");
 			if (_flag.get(flagno) == false)
 			{
 				_pgmStep += 2;
 				_branchExecuted = true;
 			}
 			if (flagno == 2 || flagno == 3)
 			{
 				// These flags are reset when tested:
 				_flag.set(flagno, false);
 			}
 			SetPendingFunction("");
 			isPendingOperand = false;
 			autolift = true;
 		}
 	}	
 	
 	public void IncrementRegisterAndSkipIfZero(String name)
 	{
 		// We'll enter this function when Shift GSB is selected (flag -1)
 		// And later when flag number is chosen (0-3).
 		if (name.length() == 0)
 		{
 			SetPendingFunction("ISZ");
 			isPendingOperand = true;
 		}
 		else
 		{
 			if (name.equals("RCLI"))
 			{
 					PrintByMode("ISZI");
 			}
 			else if (name.equals("IDX"))
 			{
 					PrintByMode("ISZ\u00a1");
 			}
 			// increment register
 			// if register == 0 add 2 to pgmStep
 			double val = GetRegister(name);
 			if (!isError)
 			{
 				val++;
 				SetRegister(name, val);
 				if (val < 1 && val > -1)
 				{
 					_pgmStep += 2;
 					_branchExecuted = true;
 				}
 				SetPendingFunction("");
 				isPendingOperand = false;
 			}
 			autolift = true;
 		}
 	}
 
 	public void DecrementRegisterAndSkipIfZero(String name)
 	{
 		// We'll enter this function when Shift GSB is selected (flag -1)
 		// And later when flag number is chosen (0-3).
 		if (name.length()==0)
 		{
 			SetPendingFunction("DSZ");
 			isPendingOperand = true;
 		}
 		else
 		{
 			if (name.equals("RCLI"))
 			{
 					PrintByMode("DSZI");
 			}
 			else if (name.equals("IDX"))
 			{
 					PrintByMode("DSZ\u00a1");
 			}
 			// decrement register
 			// if register == 0 add 2 to pgmStep
 			double val = GetRegister(name);
 			if (!isError)
 			{
 				val--;
 				SetRegister(name, val);
 				if (val<1 && val>-1)
 				{
 					_pgmStep += 2;
 					_branchExecuted = true;
 				}
 				SetPendingFunction("");
 				isPendingOperand = false;
 			}
 			autolift = true;
 		}
 	}	
 	
 	public void LabelKey(String name)
 	{
 		if (name.length() == 0)
 		{
 			SetPendingFunction("LBL");
 			isPendingOperand = true;
 			_gotoFKeyLabel = false;
 		}
 		else
 		{
 			if (name.equals("F"))
 			{
 				_gotoFKeyLabel=true;
 				return;
 			}
 			if (ValidLabelKey(name))
 			{
 				if (_gotoFKeyLabel == true)
 				{
 					if (name.equals("A"))
 					{
 							PrintByMode("*LBLa");
 					}
 					else if (name.equals("B"))
 					{
 							PrintByMode("*LBLb");
 					}
 					else if (name.equals("C"))
 					{
 							PrintByMode("*LBLc");
 					}
 					else if (name.equals("D"))
 					{
 							PrintByMode("*LBLd");
 					}
 					else if (name.equals("E"))
 					{
 							PrintByMode("*LBLe");
 					}
 				}
 				else
 				{
 					if (name.equals("ZERO"))
 					{
 							PrintByMode("*LBL0");
 					}
 					else if (name.equals("ONE"))
 					{
 							PrintByMode("*LBL1");
 					}
 					else if (name.equals("TWO"))
 					{
 							PrintByMode("*LBL2");
 					}
 					else if (name.equals("THREE"))
 					{
 							PrintByMode("*LBL3");
 					}
 					else if (name.equals("FOUR"))
 					{
 							PrintByMode("*LBL4");
 					}
 					else if (name.equals("FIVE"))
 					{
 							PrintByMode("*LBL5");
 					}
 					else if (name.equals("SIX"))
 					{
 							PrintByMode("*LBL6");
 					}
 					else if (name.equals("SEVEN"))
 					{
 							PrintByMode("*LBL7");
 					}
 					else if (name.equals("EIGHT"))
 					{
 							PrintByMode("*LBL8");
 					}
 					else if (name.equals("NINE"))
 					{
 							PrintByMode("*LBL9");
 					}
 					else if (name.equals("A"))
 					{
 							PrintByMode("*LBLA");
 					}
 					else if (name.equals("B"))
 					{
 							PrintByMode("*LBLB");
 					}
 					else if (name.equals("C"))
 					{
 							PrintByMode("*LBLC");
 					}
 					else if (name.equals("D"))
 					{
 							PrintByMode("*LBLD");
 					}
 					else if (name.equals("E"))
 					{
 							PrintByMode("*LBLE");
 					}
 				}
 				autolift = true;
 				SetPendingFunction("");
 				isPendingOperand = false;
 				if (name.equals("IDX"))
 				{
 					ProcessKeyStroke("IDX");
 				}
 			}
 			else
 			{
 				// Not a valid LBL label key
 				SetPendingFunction("");
 				isPendingOperand = false;
 				ProcessKeyStroke(name);
 				return;
 			}
 		}
 	}	
 
 	public void Store(String name)
 	{
 		if (_pendingRegMath.equals("MULT"))
 		{
 				PrintSTOMult(name);
 		}
 		else if (_pendingRegMath.equals("DIV"))
 		{
 				PrintSTODiv(name);
 				if (GetX() == 0)
 				{
 					isError = true;
 				}
 		}
 		else if (_pendingRegMath.equals("PLUS"))
 		{
 				PrintSTOPlus(name);
 		}
 		else if (_pendingRegMath.equals("SUB"))
 		{
 				PrintSTOSub(name);
 		}
 		else
 		{
 				PrintSTODefault(name);
 
 		}
 		if (!isError)
 		{
 			// Store in named register
 			SetRegister(name, GetX());
 		}
 		//PrintTrace();
 		autolift = true;
 	}	
 
 	private void PrintSTODefault(String name)
 	{
 		if (name.equals("ZERO"))
 		{
 				PrintByMode("STO0");
 		}
 		else if (name.equals("ONE"))
 		{
 				PrintByMode("STO1");
 		}
 		else if (name.equals("TWO"))
 		{
 				PrintByMode("STO2");
 		}
 		else if (name.equals("THREE"))
 		{
 				PrintByMode("STO3");
 		}
 		else if (name.equals("FOUR"))
 		{
 				PrintByMode("STO4");
 		}
 		else if (name.equals("FIVE"))
 		{
 				PrintByMode("STO5");
 		}
 		else if (name.equals("SIX"))
 		{
 				PrintByMode("STO6");
 		}
 		else if (name.equals("SEVEN"))
 		{
 				PrintByMode("STO7");
 		}
 		else if (name.equals("EIGHT"))
 		{
 				PrintByMode("STO8");
 		}
 		else if (name.equals("NINE"))
 		{
 				PrintByMode("STO9");
 		}
 		else if (name.equals("A"))
 		{
 				PrintByMode("STOA");
 		}
 		else if (name.equals("B"))
 		{
 				PrintByMode("STOB");
 		}
 		else if (name.equals("C"))
 		{
 				PrintByMode("STOC");
 		}
 		else if (name.equals("D"))
 		{
 				PrintByMode("STOD");
 		}
 		else if (name.equals("E"))
 		{
 				PrintByMode("STOE");
 		}
 		else if (name.equals("RCLI"))
 		{
 				PrintByMode("STOI");
 		}
 		else if (name.equals("IDX"))
 		{
 				PrintByMode("STO\u00a1");
 		}
 	}
 
 	private void PrintSTOSub(String name)
 	{
 		if (name.equals("ZERO"))
 		{
 				PrintByMode("ST-0");
 		}
 		else if (name.equals("ONE"))
 		{
 				PrintByMode("ST-1");
 		}
 		else if (name.equals("TWO"))
 		{
 				PrintByMode("ST-2");
 		}
 		else if (name.equals("THREE"))
 		{
 				PrintByMode("ST-3");
 		}
 		else if (name.equals("FOUR"))
 		{
 				PrintByMode("ST-4");
 		}
 		else if (name.equals("FIVE"))
 		{
 				PrintByMode("ST-5");
 		}
 		else if (name.equals("SIX"))
 		{
 				PrintByMode("ST-6");
 		}
 		else if (name.equals("SEVEN"))
 		{
 				PrintByMode("ST-7");
 		}
 		else if (name.equals("EIGHT"))
 		{
 				PrintByMode("ST-8");
 		}
 		else if (name.equals("NINE"))
 		{
 				PrintByMode("ST-9");
 		}
 		else if (name.equals("IDX"))
 		{
 				PrintByMode("ST-\u00a1");
 		}
 	}
 
 	private void PrintSTOPlus(String name)
 	{
 		if (name.equals("ZERO"))
 		{
 				PrintByMode("ST+0");
 		}
 		else if (name.equals("ONE"))
 		{
 				PrintByMode("ST+1");
 		}
 		else if (name.equals("TWO"))
 		{
 				PrintByMode("ST+2");
 		}
 		else if (name.equals("THREE"))
 		{
 				PrintByMode("ST+3");
 		}
 		else if (name.equals("FOUR"))
 		{
 				PrintByMode("ST+4");
 		}
 		else if (name.equals("FIVE"))
 		{
 				PrintByMode("ST+5");
 		}
 		else if (name.equals("SIX"))
 		{
 				PrintByMode("ST+6");
 		}
 		else if (name.equals("SEVEN"))
 		{
 				PrintByMode("ST+7");
 		}
 		else if (name.equals("EIGHT"))
 		{
 				PrintByMode("ST+8");
 		}
 		else if (name.equals("NINE"))
 		{
 				PrintByMode("ST+9");
 		}
 		else if (name.equals("IDX"))
 		{
 				PrintByMode("ST+\u00a1");
 		}
 	}
 
 	private void PrintSTODiv(String name)
 	{
 		if (name.equals("ZERO"))
 		{
 				PrintByMode("ST\u00f70");
 		}
 		else if (name.equals("ONE"))
 		{
 				PrintByMode("ST\u00f71");
 		}
 		else if (name.equals("TWO"))
 		{
 				PrintByMode("ST\u00f72");
 		}
 		else if (name.equals("THREE"))
 		{
 				PrintByMode("ST\u00f73");
 		}
 		else if (name.equals("FOUR"))
 		{
 				PrintByMode("ST\u00f74");
 		}
 		else if (name.equals("FIVE"))
 		{
 				PrintByMode("ST\u00f75");
 		}
 		else if (name.equals("SIX"))
 		{
 				PrintByMode("ST\u00f76");
 		}
 		else if (name.equals("SEVEN"))
 		{
 				PrintByMode("ST\u00f77");
 		}
 		else if (name.equals("EIGHT"))
 		{
 				PrintByMode("ST\u00f78");
 		}
 		else if (name.equals("NINE"))
 		{
 				PrintByMode("ST\u00f79");
 		}
 		else if (name.equals("IDX"))
 		{
 				PrintByMode("ST\u00f7\u00a1");
 		}
 	}	
 	
 	private void PrintSTOMult(String name)
 	{
 		if (name.equals("ZERO"))
 		{
 				PrintByMode("STx0");
 		}
 		else if (name.equals("ONE"))
 		{
 				PrintByMode("STx1");
 		}
 		else if (name.equals("TWO"))
 		{
 				PrintByMode("STx2");
 		}
 		else if (name.equals("THREE"))
 		{
 				PrintByMode("STx3");
 		}
 		else if (name.equals("FOUR"))
 		{
 				PrintByMode("STx4");
 		}
 		else if (name.equals("FIVE"))
 		{
 				PrintByMode("STx5");
 		}
 		else if (name.equals("SIX"))
 		{
 				PrintByMode("STx6");
 		}
 		else if (name.equals("SEVEN"))
 		{
 				PrintByMode("STx7");
 		}
 		else if (name.equals("EIGHT"))
 		{
 				PrintByMode("STx8");
 		}
 		else if (name.equals("NINE"))
 		{
 				PrintByMode("STx9");
 		}
 		else if (name.equals("IDX"))
 		{
 				PrintByMode("STOx\u00a1");
 		}
 	}
 
 	public void Recall(String name)
 	{
 		if (name.equals("ZERO"))
 		{
 				PrintByMode("RCL0");
 		}
 		else if (name.equals("ONE"))
 		{
 				PrintByMode("RCL1");
 		}
 		else if (name.equals("TWO"))
 		{
 				PrintByMode("RCL2");
 		}
 		else if (name.equals("THREE"))
 		{
 				PrintByMode("RCL3");
 		}
 		else if (name.equals("FOUR"))
 		{
 				PrintByMode("RCL4");
 		}
 		else if (name.equals("FIVE"))
 		{
 				PrintByMode("RCL5");
 		}
 		else if (name.equals("SIX"))
 		{
 				PrintByMode("RCL6");
 		}
 		else if (name.equals("SEVEN"))
 		{
 				PrintByMode("RCL7");
 		}
 		else if (name.equals("EIGHT"))
 		{
 				PrintByMode("RCL8");
 		}
 		else if (name.equals("NINE"))
 		{
 				PrintByMode("RCL9");
 		}
 		else if (name.equals("A"))
 		{
 				PrintByMode("RCLA");
 		}
 		else if (name.equals("B"))
 		{
 				PrintByMode("RCLB");
 		}
 		else if (name.equals("C"))
 		{
 				PrintByMode("RCLC");
 		}
 		else if (name.equals("D"))
 		{
 				PrintByMode("RCLD");
 		}
 		else if (name.equals("E"))
 		{
 				PrintByMode("RCLE");
 		}
 		else if (name.equals("SUMPLUS"))
 		{
 				PrintByMode("RCL\u03a3");
 		}
 		else if (name.equals("RCLI"))
 		{
 				PrintByMode("RCLI");
 		}
 		else if (name.equals("IDX"))
 		{
 				PrintByMode("RCL\u00a1");
 		}
 		// Store in named register
 		double tmp;
 		if (name.equals("SUMPLUS"))
 		{
 			SetY(_register.get(16));
 			SetLastX(GetX());
 			SetX(_register.get(14));
 		}
 		else
 		{
 			tmp = GetRegister(name);
 			if (!isError)
 			{
 				if (autolift == true || isEnteringData == true)
 				{
 					LiftUpStack();
 				}
 				SetX(tmp);
 			}
 		}
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void RunStop()
 	{
 		PrintByMode("R/S");
 		if (GetRunning())
 		{
 			SetRunning(false);
 		}
 		else
 		{
 			// If next step is not another R/S, then do step
 			if (!_program.get(_pgmStep).getKeyCode1().equals("51"))
 			{
 				SetRunning(true);
 				Thread thr = new Thread()
 				{
 					public void run()
 					{
 					    RunSubRoutine();	
 					}
 				};
 				thr.start();
 			}
 		}
 		autolift = true;
 		//isEnteringData = false;
 		//ResetDigitFlags();
 	}	
 	
 	public void GoSubShortcut(boolean shifted, String label)
 	{
 		// I don't believe we should ever be able to be running a program
 		// when this method is called - since a single function key isn't
 		// a programmable step...
 		// So, let's make sure we go back to a starting state before
 		// kicking this off...
 		_gotoFKeyLabel = shifted;
 		SetRunning(false);
 		NextStep.clear();
 		_highestStackCount = 0;
 
 		// Now, let's go!
 		if (shifted)
 		{
 			_gotoFKeyLabel = true;
 		}
 		GoSub(label);
 	}	
 	
 	public void ReturnSub()
 	{
 		// Trace
 		//Console.WriteLine(String.Format(CI,"{0:0}-{1:000} ",_highestStackCount, _pgmStep) + 
 		//                  "Depth: "+ NextStep.Count +
 		//                  " PeekStep: "+NextStep.Peek());
 
 		// If there are stacked calls
 		if (NextStep.size() > Math.max(_highestStackCount - MaxNesting,1))
 		{
 			_pgmStep = PopNextStep();
 		}
 		else
 		{
 			SetRunning(false);
 			NextStep.clear();
 			_highestStackCount = 0;
 		}
 		autolift = true;
 	}	
 	
 	public void BSTDecrementProgramStep()
 	{
 		autolift = true;
 	}
 
 	public void SSTExecuteProgramStep()
 	{
 		if (GetPrintMode() == printModes.Trace)
 		{
 			String line;
 			line = String.format("%3s%7s", String.format("%03d", _pgmStep), _program.get(_pgmStep).getLabel());
 			RenderLine(line);
 			Print();
 		}
 
 		// Execute program step
 		if (!GetRunning())
 		{
 			// If we're single stepping manually execute async
 			FlutterDisplay();
 			ExecuteCurrentProgramStep();			
 			_isRandomDisplay = false;
 			DisplayUpdate();
 			if (!_branchExecuted)
 			{
 				_pgmStep += 1;
 			}
 			if (_pgmStep > MaxProgramSteps)
 			{
 				_pgmStep = 1;
 			}
 //			Thread thr = new Thread()
 //			{
 //				public void run()
 //				{
 //					ExecuteCurrentProgramStep();
 //				}
 //			};
 //			thr.start();
 		}
 		else
 		{
 			// If free running - execute sync
 			FlutterDisplay();
 			ExecuteCurrentProgramStep();
 			_isRandomDisplay = false;
 			DisplayUpdate();
 			if (!_branchExecuted)
 			{
 				_pgmStep += 1;
 			}
 			if (_pgmStep > MaxProgramSteps)
 			{
 				_pgmStep = 1;
 			}
 			try {
 				Thread.sleep(ProgramStepDelayMilli);
 			} catch (InterruptedException e) {
 			}
 		}
 	}	
 	
     private void FlutterDisplay()
     {
     	try
     	{
             _isRandomDisplay = true;
             _randomDisplay = "-0.000000000-00";
             DisplayUpdate();
             Thread.sleep(5);
             _randomDisplay = "-0.008000008-00";
             DisplayUpdate();
             Thread.sleep(5);
             _randomDisplay = "-0.800808882-00";
             DisplayUpdate();
             Thread.sleep(5);
             _randomDisplay = "-30038330063-23";
             DisplayUpdate();
             Thread.sleep(5);
             _isRandomDisplay = false;
     	}
     	catch (Exception ex)
     	{
     		
     	}
     }
 
 	public void Add()
 	{
 		PrintByMode("+ ");
 		double result = GetY() + GetX();
 		SetLastX(GetX());
 		ShiftDownStack();
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void Subtract()
 	{
 		PrintByMode("- ");
 		double result = GetY() - GetX();
 		SetLastX(GetX());
 		ShiftDownStack();
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void Multiply()
 	{
 		PrintByMode("x ");
 		double result = GetY() * GetX();
 		SetLastX(GetX());
 		ShiftDownStack();
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void Divide()
 	{
 		PrintByMode("\u00f7 ");
 		if (GetX() != 0)
 		{
 			double result = GetY() / GetX();
 			SetLastX(GetX());
 			ShiftDownStack();
 			SetX(result);
 			PrintTrace();
 		}
 		else
 		{
 			isError = true;
 			PrintTrace();
 		}
 		autolift = true;
 	}	
 	
 	public void PrintX()
 	{
 		RenderLine(FormatPrintLine());
 		autolift = true;
 		Print();
 	}
 
 	public void ClearX()
 	{
 		PrintByMode("CLX");
 		SetX(0);
 		autolift = false;
 		ResetDigitFlags();
 		PrintTrace();
 	}
 
 	public void Enter()
 	{
 		PrintByMode("ENT\u2191");
 		LiftUpStack();
 		autolift = false;
 		ResetDigitFlags();
 	}	
 	
 	public void ShowLastX()
 	{
 		PrintByMode("LSTX");
 		if (autolift == true || isEnteringData == true)
 		{
 			LiftUpStack();
 		}
 		SetX(GetLastX());
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void SwapXY()
 	{
 		PrintByMode("X\u2195Y");
 		double tmp = GetX();
 		SetX(GetY());
 		SetY(tmp);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void SwapXI()
 	{
 		PrintByMode("X\u2195I");
 		double tmp = GetX();
 		SetX(GetI());
 		SetI(tmp);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void RollDownStack()
 	{
 		PrintByMode("R\u2193");
 		double tmp = GetX();
 		SetX(GetY());
 		SetY(GetZ());
 		SetZ(GetT());
 		SetT(tmp);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void RollUpStack()
 	{
 		PrintByMode("R\u2191");
 		double tmp = GetT();
 		SetT(GetZ());
 		SetZ(GetY());
 		SetY(GetX());
 		SetX(tmp);
 		autolift = true;
 		PrintTrace();
 	}
 	
 	public void RecallI()
 	{
 		PrintByMode("RCLI");
 		if (autolift == true || isEnteringData == true)
 		{
 			LiftUpStack();
 		}
 		SetX(GetI());
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void NegateX()
 	{
 		if (!isEnteringData)
 		{
 			PrintByMode("CHS");
 		}
 		if (isEnteringData)
 		{
 			if (isLeadingDecimal)
 			{
 				return;
 			}
 			else
 			{
 				if (!_inputDigits.contains("-"))
 				{
 					_inputDigits = "-" + _inputDigits;
 				}
 				else
 				{
 					_inputDigits = _inputDigits.substring(1);
 				}
 				SetX(Double.parseDouble(_inputDigits));
 			}
 		}
 		else
 		{
 			SetX(GetX() * -1);
 		}
 		if (!isEnteringData)
 		{
 			PrintTrace();
 		}
 	}	
 	
 	public void LN()
 	{
 		PrintByMode("LN");
 		double result;
 		if (GetX() == 0)
 		{
 			isError = true;
 		}
 		else
 		{
 			result = Math.log(GetX());
 			SetLastX(GetX());
 			SetX(result);
 		}
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void LOG10()
 	{
 		PrintByMode("LOG");
 		double result;
 		if (GetX() == 0)
 		{
 			isError = true;
 		}
 		else
 		{
 			result = Math.log10(GetX());
 			SetLastX(GetX());
 			SetX(result);
 		}
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void EXP()
 	{
 		PrintByMode("e\u00aa");
 		double result;
 		result = Math.exp(GetX());
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void TENX()
 	{
 		PrintByMode("10\u00aa");
 		double result;
 		result = Math.pow(10,GetX());
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void Sin()
 	{
 		PrintByMode("SIN");
 		double result;
 		if (GetX() == 180)
 		{
 			result = 0;
 		}
 		else
 		{
 			result = Math.sin(ToRads(GetX()));
 		}
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}	
 
 	public void ArcSin()
 	{
 		PrintByMode("SIN\u00b9");
 		double result = 0;
 		if (Math.abs(GetX()) > 1)
 		{
 			isError = true;
 		}
 		else
 		{
 			result = ToDegrees(Math.asin(GetX()));
 			if (Double.isNaN(result))
 			{
 				isError = true;
 			}
 			else
 			{
 				SetLastX(GetX());
 				SetX(result);
 			}
 		}
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void Cos()
 	{
 		PrintByMode("COS");
 		double result;
 		if (GetX() == 90 || GetX() == 270)
 		{
 			result = 0;
 		}
 		else
 		{
 			result = Math.cos(ToRads(GetX()));
 		}
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void ArcCos()
 	{
 		PrintByMode("COS\u00b9");
 		double result = 0;
 		if (Math.abs(GetX()) > 1)
 		{
 			isError = true;
 		}
 		else
 		{
 			result = ToDegrees(Math.acos(GetX()));
 			if (Double.isNaN(result))
 			{
 				isError = true;
 			}
 			else
 			{
 				SetLastX(GetX());
 				SetX(result);
 			}
 		}
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void Tan()
 	{
 		PrintByMode("TAN");
 		double result;
 		if (GetX() == 90 || GetX() == 270)
 		{
 			result = 9.999999999e+99;
 		}
 		else
 		{
 		if (GetX() == 180)
 		{
 			result = 0;
 		}
 		else
 		{
 			result = Math.tan(ToRads(GetX()));
 		}
 		}
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}	
 
 	public void ArcTan()
 	{
 		PrintByMode("TAN\u00b9");
 		double result;
 		result = ToDegrees(Math.atan(GetX()));
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void DegToRad()
 	{
 		PrintByMode("D\u2192R");
 		double result = Math.PI * GetX() / 180;
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void RadToDeg()
 	{
 		PrintByMode("R\u2192D");
 		double result = 180 * GetX() / Math.PI;
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void PercentOf()
 	{
 		PrintByMode("% ");
 		double result = GetY() * (GetX()/100);
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void YToXPower()
 	{
 		PrintByMode("Y\u00aa");
 		if ((GetY() < 0 && (Math.floor(GetX()) != GetX())) || (GetY() == 0 && (GetX() <= 0)))
 		{
 			isError = true;
 		}
 		else
 		{
 			double result = Math.pow(GetY(), GetX());
 			SetLastX(GetX());
 			ShiftDownStack();
 			SetX(result);
 		}
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void PI()
 	{
 		PrintByMode("P\u00a1");
 		if (autolift == true || isEnteringData == true)
 		{
 			LiftUpStack();
 		}
 		SetX(3.141592654);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void Square()
 	{
 		PrintByMode("X\u00b2");
 		double result = GetX() * GetX();
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}	
 	
 	public void Int()
 	{
 		PrintByMode("INT");
 		double result = Math2.Truncate(GetX());
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void Frac()
 	{
 		PrintByMode("FRC");
 		double result = Math.round((GetX() - Math2.Truncate(GetX())) * Math.pow(10, 10)) / Math.pow(10, 10);
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void Round()
 	{
 		PrintByMode("RND");
 		double result = Math.round((GetX()) * Math.pow(10, (int)_displayDigitCount)) / Math.pow(10, (int)_displayDigitCount);
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void ABS()
 	{
 		PrintByMode("ABS");
 		double result = Math.abs(GetX());
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void Reciprocal()
 	{
 		PrintByMode("1/X");
 		if (GetX() == 0)
 		{
 			isError = true;
 		}
 		else
 		{
 			double result = 1 / GetX();
 			SetLastX(GetX());
 			SetX(result);
 		}
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void SquareRoot()
 	{
 		PrintByMode("\u221aX");
 		if (GetX() < 0)
 		{
 			isError = true;
 		}
 		else
 		{
 			double result = Math.sqrt(GetX());
 			SetLastX(GetX());
 			SetX(result);
 		}
 		autolift = true;
 		PrintTrace();
 	}
 	
 	private double ToRads(double X)
 	{
 		double rads=0;
 		switch (_angleMode)
 		{
 			case Degrees:
 				rads = Math.PI*X/180;
 				break;
 			case Radians:
 				rads = X;
 				break;
 			case Grads:
 				rads = Math.PI*X/200;
 				break;
 		}
 		return rads;
 	}
 
 	private double ToDegrees(double X)
 	{
 		double degs = 0;
 		switch (_angleMode)
 		{
 			case Degrees:
 				degs = 180*X / Math.PI;
 				break;
 			case Radians:
 				degs = X;
 				break;
 			case Grads:
 				degs = 200*X / Math.PI;
 				break;
 		}
 		return degs;
 	}
 	
 	public void ToPolar()
 	{
 		PrintByMode("\u2192P");
 		boolean xneg = (GetX() < 0) ? true : false;
 		boolean yneg = (GetY() < 0) ? true : false;
 		double degs = ToDegrees(Math.atan(GetY() / GetX()));
 		double mag = Math.sqrt(Math.pow(GetX(),2) + Math.pow(GetY(),2));
 
 		if (xneg && !yneg)
 		{
 			switch (_angleMode)
 			{
 				case Degrees:
 					SetY((degs < 0) ? 180 + degs : degs);
 					break;
 				case Radians:
 					SetY((degs < 0) ? Math.PI + degs : degs);
 					break;
 				case Grads:
 					SetY((degs < 0) ? 200 + degs : degs);
 					break;
 			}
 		}
 		if (xneg && yneg)
 		{
 			switch (_angleMode)
 			{
 				case Degrees:
 					SetY((degs > 0) ? -180 + degs : degs);
 					break;
 				case Radians:
 					SetY((degs > 0) ? -Math.PI + degs : degs);
 					break;
 				case Grads:
 					SetY((degs > 0) ? -200 + degs : degs);
 					break;
 			}
 		}
 		if ((!xneg && yneg)||(!xneg && !yneg))
 		{
 			SetY(degs);
 		}
 		SetLastX(GetX());
 		SetX(mag);
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void ToRect()
 	{
 		PrintByMode("\u2192R");
 		double x = Math.cos(ToRads(GetY()))*GetX();
 		double y = Math.sin(ToRads(GetY()))*GetX();
 		if (Double.isNaN(x) || Double.isNaN(y))
 		{
 			x = 0;
 			y = 0;
 		}
 		SetY(y);
 		SetLastX(GetX());
 		SetX(x);
 		autolift = true;
 		PrintTrace();
 	}	
 	
 	public void SumPlus()
 	{
 		PrintByMode("\u03a3+");
 		_register.set(14, _register.get(14) + GetX());
 		_register.set(15, _register.get(15) + Math.pow(GetX(), 2));
 		_register.set(16, _register.get(16) + GetY());
 		_register.set(17, _register.get(17) + Math.pow(GetY(), 2));
 		_register.set(18, _register.get(18) + (GetX() * GetY()));
 		_register.set(19, _register.get(19) + 1);
 		SetLastX(GetX());
 		SetX(_register.get(19));
 		autolift = false;
 		PrintTrace();
 	}
 
 	public void SumMinus()
 	{
 		PrintByMode("\u03a3-");
 		_register.set(14, _register.get(14) - GetX());
 		_register.set(15, _register.get(15) - Math.pow(GetX(), 2));
 		_register.set(16, _register.get(16) - GetY());
 		_register.set(17, _register.get(17) - Math.pow(GetY(), 2));
 		_register.set(18, _register.get(18) - (GetX() * GetY()));
 		_register.set(19, _register.get(19) - 1);
 		SetLastX(GetX());
 		SetX(_register.get(19));
 		autolift = false;
 		PrintTrace();
 	}	
 
 	public void XNotEqualY()
 	{
 		PrintByMode("X\u2260Y?");
 		if (GetX() == GetY())
 		{
 			_pgmStep += 2;
 			_branchExecuted = true;
 		}
 		autolift = true;
 	}
 
 	public void XEqualY()
 	{
 		PrintByMode("X=Y?");
 		if (GetX() != GetY())
 		{
 			_pgmStep += 2;
 			_branchExecuted = true;
 		}
 		autolift = true;
 	}
 
 	public void XGreaterThanY()
 	{
 		PrintByMode("X>Y?");
 		if (GetX() <= GetY())
 		{
 			_pgmStep += 2;
 			_branchExecuted = true;
 		}
 		autolift = true;
 	}
 
 	public void XLessThanEqualY()
 	{
 		PrintByMode("X\u2264Y?");
 		if (GetX() > GetY())
 		{
 			_pgmStep += 2;
 			_branchExecuted = true;
 		}
 		autolift = true;
 	}
 
 	public void XGreaterThanZero()
 	{
 		PrintByMode("X>0?");
 		if (GetX() <= 0)
 		{
 			_pgmStep += 2;
 			_branchExecuted = true;
 		}
 		autolift = true;
 	}
 
 	public void XLessThanZero()
 	{
 		PrintByMode("X<0?");
 		if (GetX() >= 0)
 		{
 			_pgmStep += 2;
 			_branchExecuted = true;
 		}
 		autolift = true;
 	}
 
 	public void XNotEqualZero()
 	{
 		PrintByMode("X\u22600?");
 		if (GetX() == 0)
 		{
 			_pgmStep += 2;
 			_branchExecuted = true;
 		}
 		autolift = true;
 	}
 
 	public void XEqualZero()
 	{
 		PrintByMode("X=0?");
 		if (GetX() != 0)
 		{
 			_pgmStep += 2;
 			_branchExecuted = true;
 		}
 		autolift = true;
 	}	
 	
 	public void PrintProgram()
 	{
 		String line;
 		int lastLine = 0;
 		for (int i = MaxProgramSteps; i >= 0; i--)
 		{
 			if (!_program.get(i).getKeyCode1().equals("51"))
 			{
 				lastLine = i + 1;
 				break;
 			}
 		}
 		for (int i = _pgmStep; i <= Math.min(lastLine,MaxProgramSteps); i++)
 		{
 			if (i == 0)
 			{
 				continue;
 			}
 			if (GetPrintMode() == printModes.Manual)
 			{
 				line = String.format("%3s%7s%4s%3s%3s", String.format("%03d", i), _program.get(i).getLabel(), _program.get(i).getKeyCode3(), 
 						_program.get(i).getKeyCode2(), _program.get(i).getKeyCode1());
 			}
 			else
 			{
 				line = String.format("%3s%7s", String.format("%03d", i), _program.get(i).getLabel());
 			}
 			RenderLine(line);
 		}
 		if (!GetHP67PrintMode())
 		{
 			autolift = true;
 			Print();
 		}
 	}	
 	
 	public void PrintStack()
 	{
 		double saveX = GetX();
 		String line;
 		isEnteringData = false;
 		autolift = true;
 		if (GetHP67PrintMode())
 		{
 			SetStopPrint(false);
 			SetX(GetT());
 			PrintStackBlink();
 			PrintStackLine();
 			SetX(GetZ());
 			PrintStackBlink();
 			PrintStackLine();
 			SetX(GetY());
 			PrintStackBlink();
 			PrintStackLine();
 			SetX(saveX);
 			PrintStackBlink();
 			PrintStackLine();
 			SetX(saveX);
 			DisplayUpdate();
 			PrintStackLine();
 			DisplayUpdate();
 		}
 		else
 		{
 			PrintByMode("PRST");
 			RenderLine("");
 			SetX(GetT());
 			line = FormatPrintLine();
 			line += " T ";
 			line = line.replace("***", "");
 			RenderLine(line);
 			SetX(GetZ());
 			line = FormatPrintLine();
 			line += " Z ";
 			line = line.replace("***", "");
 			RenderLine(line);
 			SetX(GetY());
 			line = FormatPrintLine();
 			line += " Y ";
 			line = line.replace("***", "");
 			RenderLine(line);
 			SetX(saveX);
 			line = FormatPrintLine();
 			line += " X ";
 			line = line.replace("***", "");
 			RenderLine(line);
 			RenderLine("");
 			autolift = true;
 			Print();
 		}
 	}	
 	
 	public void PrintStackBlink()
 	{
 		// There will be a value in X to display
 		for (int i = 0; i < 6; i++)
 		{
 			if (decimalOn == false)
 			{
 				decimalOn = true;
 			}
 			else
 			{
 				decimalOn = false;
 			}
 			DisplayUpdate();
 			try {
 				Thread.sleep(400); // half second sleep
 			} catch (InterruptedException e) {
 			}
 		}
 		decimalOn = true;
 		DisplayUpdate();
 	}	
 
 	public boolean PrintStackLine()
 	{
 		try {
 			Thread.sleep(200); //.5 second sleep
 		} catch (InterruptedException e) {
 		}
 		DisplayUpdate();
 		_isRandomDisplay = true;
 		_randomDisplay = String.format("%15s", "    ");
 		DisplayUpdate();
 		try {
 			Thread.sleep(200);
 		} catch (InterruptedException e) {
 		}
 		_isRandomDisplay = false;
 		return false;
 	}
 	
 	
 	
 	private void PrintHP97Registers(double saveX)
 	{
 		String line;
 		RenderLine("");
 		SetX(_register.get(0));
 		line = FormatPrintLine();
 		line += " 0 ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(1));
 		line = FormatPrintLine();
 		line += " 1 ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(2));
 		line = FormatPrintLine();
 		line += " 2 ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(3));
 		line = FormatPrintLine();
 		line += " 3 ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(4));
 		line = FormatPrintLine();
 		line += " 4 ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(5));
 		line = FormatPrintLine();
 		line += " 5 ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(6));
 		line = FormatPrintLine();
 		line += " 6 ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(7));
 		line = FormatPrintLine();
 		line += " 7 ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(8));
 		line = FormatPrintLine();
 		line += " 8 ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(9));
 		line = FormatPrintLine();
 		line += " 9 ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(20));
 		line = FormatPrintLine();
 		line += " A ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(21));
 		line = FormatPrintLine();
 		line += " B ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(22));
 		line = FormatPrintLine();
 		line += " C ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(23));
 		line = FormatPrintLine();
 		line += " D ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(24));
 		line = FormatPrintLine();
 		line += " E ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		SetX(_register.get(25));
 		line = FormatPrintLine();
 		line += " I ";
 		line = line.replace("***", "");
 		RenderLine(line);
 		RenderLine("");
 		SetX(saveX);
 		autolift = true;
 		Print();
 	}	
 	
 	public void PrintRegisters()
 	{
 		PrintByMode("PREG");
 		isEnteringData = false;
 		double saveX = GetX();
 		autolift = true;
 		String line;
 		if (GetHP67PrintMode())
 		{
 			SetStopPrint(false);
 			for (int i=0;i<10;i++)
 			{
 				_isRandomDisplay = true;
 				_randomDisplay = String.format("%15s", i);
 				DisplayUpdate();
 				try {
 					Thread.sleep(750);
 				} catch (InterruptedException e) {
 				}
 				_isRandomDisplay = false;
 				SetX(_register.get(i));
 				if (PrintRegisterLine())
 				{
 					SetX(saveX);
 					DisplayUpdate();
 					return;
 				}
 			}
 			for (int i=20;i<26;i++)
 			{
 				_isRandomDisplay = true;
 				_randomDisplay = String.format("%15s", i);
 				DisplayUpdate();
 				try {
 					Thread.sleep(750);
 				} catch (InterruptedException e) {
 				}
 				_isRandomDisplay = false;
 				SetX(_register.get(i));
 				if (PrintRegisterLine())
 				{
 					SetX(saveX);
 					DisplayUpdate();
 					return;
 				}
 			}
 			SetX(saveX);
 			DisplayUpdate();
 		}
 		else
 		{
 			PrintHP97Registers(saveX);
 		}
 	}	
 
 	public boolean PrintRegisterLine()
 	{
 		boolean stop=GetStopPrint();
 		DisplayUpdate();
 		try {
 			Thread.sleep(1000); //.5 second sleep
 		} catch (InterruptedException e) {
 		}
 		return stop;
 	}	
 	
 	public void ClearRegisters()
 	{
 		PrintByMode("CLRG");
 		// Clear primary 0-9 and A-I registers
 		for (int i = 0; i < 10; i++)
 		{
 			_register.set(i, 0.0);
 		}
 		for (int i = 20; i < 26; i++)
 		{
 			_register.set(i, 0.0);
 		}
 		autolift = true;
 	}	
 
 	public void Factorial()
 	{
 		PrintByMode("N!");
 		double result=1;
 		SetLastX(GetX());
 		if (GetX() <= 69)
 		{
 			if (GetX() - Math2.Truncate(GetX()) > 0||GetX()<0)
 			{
 				isError = true;
 				PrintTrace();
 				return;
 			}
 			for (int i = 1; i <= GetX(); i++)
 			{
 				result *= i;
 			}
 			SetX(result);
 		}
 		else
 		{
 			SetX(1E100);
 		}
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void PercentChg()
 	{
 		PrintByMode("%CH");
 		if (GetY() == 0)
 		{
 			isError = true;
 		}
 		else
 		{
 			double result = ((GetX() - GetY()) * 100) / GetY();
 			SetLastX(GetX());
 			SetX(result);
 		}
 		autolift = true;
 		PrintTrace();
 	}	
 	
 	public void SwapRegs()
 	{
 		PrintByMode("P\u2195S");
 		double tmp;
 		for (int i = 0; i < 10; i++)
 		{
 			tmp = _register.get(i);
 			_register.set(i, _register.get(i + 10));
 			_register.set(i + 10, tmp);
 		}
 		autolift = true;
 	}	
 
 	public void ToHMS(boolean trace)
 	{
 		if (trace)
 		{
 			PrintByMode("\u2192HMS");
 		}
 		double hours = Math2.Truncate(GetX());
 		double hoursfrac = GetX() - hours;
 		double mins1 = Math.round((hoursfrac * 60) * Math.pow(10, 10)) / Math.pow(10, 10);
 		double minsfrac = mins1 - Math2.Truncate(mins1);
 		double secs = Math.round((minsfrac * 60) * Math.pow(10, 10)) / Math.pow(10, 10);
 		double shiftmins = Math2.Truncate(mins1) / 100;
 		double shiftsecs = secs / 10000;
 		double result = hours + shiftmins + shiftsecs;
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		if (trace)
 		{
 			PrintTrace();
 		}
 	}
 
 	public void HMSTo(boolean trace)
 	{
 		if (trace)
 		{
 			PrintByMode("HMS\u2192");
 		}
 		double hours = Math2.Truncate(GetX());
 		double fracminssecs = Math.round((GetX() - hours) * Math.pow(10, 10)) / Math.pow(10, 10);
 		double mins = Math2.Truncate(fracminssecs * 100);
 		double secs = Math.round(((100 * fracminssecs) - mins) * Math.pow(10, 10)) / Math.pow(10, 10)*100;
 		double result = hours + (mins+secs/60)/60;
 		SetLastX(GetX());
 		SetX(result);
 		autolift = true;
 		if (trace)
 		{
 			PrintTrace();
 		}
 	}
 
 	public void HMSPlus()
 	{
 		PrintByMode("HMS+");
 		double last=GetX();
 		HMSTo(false);
 		double r1 = GetX();
 		SetX(GetY());
 		HMSTo(false);
 		double r2 = GetX();
 		SetX((r1+r2));
 		ShiftDownStack();
 		ToHMS(false);
 		SetLastX(last);
 		autolift = true;
 		PrintTrace();
 	}	
 
 	public void Mean()
 	{
 		PrintByMode("x\u02c9");
 		if (_register.get(19) == 0)
 		{
 			isError = true;
 		}
 		else
 		{
 			SetY(_register.get(16) / _register.get(19));
 			SetLastX(GetX());
 			SetX(_register.get(14) / _register.get(19));
 		}
 		autolift = true;
 		PrintTrace();
 	}
 
 	public void StdDev()
 	{
 		PrintByMode("S ");
 		double sdevx;
 		double sdevy;
 		if (_register.get(19) == 0)
 		{
 			isError = true;
 		}
 		else
 		{
 			sdevx = Math.sqrt((_register.get(15) - (Math.pow(_register.get(14), 2) / _register.get(19))) / (_register.get(19) - 1));
 			sdevy = Math.sqrt((_register.get(17) - (Math.pow(_register.get(16), 2) / _register.get(19))) / (_register.get(19) - 1));
 			if (Double.isNaN(sdevx) || Double.isNaN(sdevy))
 			{
 				isError = true;
 				PrintTrace();
 				return;
 			}
 			SetY(sdevy);
 			SetLastX(GetX());
 			SetX(sdevx);
 		}
 		autolift = true;
 		PrintTrace();
 	}	
 	
 	public void Pause()
 	{
 		PrintByMode("PSE");
 		_isRandomDisplay = false;
 		SetDoPause(true);
 		autolift = true;
 		DisplayUpdate();
 		if (!(GetPrintMode() == printModes.Manual))
 		{
 			PrintTrace();
 		}
 	}	
 
 	public void PrintHP67Program()
 	{
 		int currStep = _pgmStep;
 		// Always print full listing
 		_pgmStep = 0;
 		PrintProgram();
 		// Reset to current
 		_pgmStep = currStep;
 	}	
 
 	public void RunSubRoutine()
 	{
 		// We want to avoid overlapping subroutine execution.
 		// So this section is guarded.
 		while (GetRunning())
 		{
 			SSTExecuteProgramStep();
 			if (GetDoPause())
 			{
 				try {
 					Thread.sleep(PauseTimeMilli);
 				} catch (InterruptedException e) {
 				}
 				if (GetExtendPause())
 				{
 					// This is a bit of a hack, but comes close
 					// to emulating the extended pause feature
 					// when user hits key while pause is in action
 					try {
 						Thread.sleep(PauseTimeMilli+500);
 					} catch (InterruptedException e) {
 					}
 				}
 				SetDoPause(false);
 				SetExtendPause(false);
 			}
 			if (isError)
 			{
 				SetRunning(false);
 				NextStep.clear();
 				_highestStackCount = 0;
 				DisplayUpdate();
 			}
 		}
 
 		try {
 			Thread.sleep(SubroutineDelayMilli);
 		} catch (InterruptedException e) {
 		}
 	}	
 	
 	public synchronized void ExecuteCurrentProgramStep()
 	{
 		// We want to avoid overlapping program instruction execution;
 		// so, this section is guarded.
 		// Trace
 		//Console.WriteLine(String.Format(CI,"{0:0}-{1:000} ", NextStep.Count, _pgmStep) + _program[_pgmStep].Label);
 
 		_branchExecuted = false;
 		PgmInstruction p = _program.get(_pgmStep);
 		String keyname;
 		if (p.getKeyCode3().length() > 0)
 		{
 			keyname = GetKey(p.getKeyCode3());
 			ProcessKeyStroke(keyname);
 		}
 		if (p.getKeyCode2().length() > 0)
 		{
 			keyname = GetKey(p.getKeyCode2());
 			ProcessKeyStroke(keyname);
 		}
 		if (p.getKeyCode1().length() > 0)
 		{
 			keyname = GetKey(p.getKeyCode1());
 			ProcessKeyStroke(keyname);
 		}
 
 	}    
 	
     public String GetKey(String name)
 	{
 		if (name.equals("11"))
 		{
 				return "A";
 		}
 		else if (name.equals("12"))
 		{
 				return "B";
 		}
 		else if (name.equals("13"))
 		{
 				return "C";
 		}
 		else if (name.equals("14"))
 		{
 				return "D";
 		}
 		else if (name.equals("15"))
 		{
 				return "E";
 		}
 		else if (name.equals("16"))
 		{
 				return "F";
 		}
 		else if (name.equals("21"))
 		{
 				return "LBL";
 		}
 		else if (name.equals("22"))
 		{
 				return "GTO";
 		}
 		else if (name.equals("23"))
 		{
 				return "GSB";
 		}
 		else if (name.equals("24"))
 		{
 				return "RTN";
 		}
 		else if (name.equals("25"))
 		{
 				return "BST";
 		}
 		else if (name.equals("26"))
 		{
 				return "SST";
 		}
 		else if (name.equals("31"))
 		{
 				return "YX";
 		}
 		else if (name.equals("32"))
 		{
 				return "LN";
 		}
 		else if (name.equals("33"))
 		{
 				return "EX";
 		}
 		else if (name.equals("34"))
 		{
 				return "TOPOLAR";
 		}
 		else if (name.equals("35"))
 		{
 				return "STO";
 		}
 		else if (name.equals("36"))
 		{
 				return "RCL";
 		}
 		else if (name.equals("41"))
 		{
 				return "SIN";
 		}
 		else if (name.equals("42"))
 		{
 				return "COS";
 		}
 		else if (name.equals("43"))
 		{
 				return "TAN";
 		}
 		else if (name.equals("44"))
 		{
 				return "TORECT";
 		}
 		else if (name.equals("45"))
 		{
 				return "IDX";
 		}
 		else if (name.equals("46"))
 		{
 				return "RCLI";
 		}
 		else if (name.equals("51"))
 		{
 				return "RS";
 		}
 		else if (name.equals("52"))
 		{
 				return "RECIP";
 		}
 		else if (name.equals("53"))
 		{
 				return "SQUARE";
 		}
 		else if (name.equals("54"))
 		{
 				return "SQR";
 		}
 		else if (name.equals("55"))
 		{
 				return "PERC";
 		}
 		else if (name.equals("56"))
 		{
 				return "SUMPLUS";
 		}
 		else if (name.equals("-14"))
 		{
 				return "PRINT";
 		}
 		else if (name.equals("-21"))
 		{
 				return "ENTER";
 		}
 		else if (name.equals("-55"))
 		{
 				return "PLUS";
 		}
 		else if (name.equals("-31"))
 		{
 				return "RDOWN";
 		}
 		else if (name.equals("-41"))
 		{
 				return "XY";
 		}
 		else if (name.equals("-51"))
 		{
 				return "CLX";
 		}
 		else if (name.equals("-11"))
 		{
 				return "FIX";
 		}
 		else if (name.equals("-12"))
 		{
 				return "SCI";
 		}
 		else if (name.equals("-13"))
 		{
 				return "ENG";
 		}
 		else if (name.equals("-22"))
 		{
 				return "CHS";
 		}
 		else if (name.equals("-23"))
 		{
 				return "EEX";
 		}
 		else if (name.equals("-24"))
 		{
 				return "DIV";
 		}
 		else if (name.equals("-45"))
 		{
 				return "SUB";
 		}
 		else if (name.equals("-35"))
 		{
 				return "MULT";
 		}
 		else if (name.equals("-61"))
 		{
 				return "ZERO";
 		}
 		else if (name.equals("-53"))
 		{
 				return "TWO";
 		}
 		else if (name.equals("-42"))
 		{
 				return "FOUR";
 		}
 		else if (name.equals("-43"))
 		{
 				return "FIVE";
 		}
 		else if (name.equals("-44"))
 		{
 				return "SIX";
 		}
 		else if (name.equals("-32"))
 		{
 				return "SEVEN";
 		}
 		else if (name.equals("-33"))
 		{
 				return "EIGHT";
 		}
 		else if (name.equals("-34"))
 		{
 				return "NINE";
 		}
 		else if (name.equals("09"))
 		{
 				return "NINE";
 		}
 		else if (name.equals("00"))
 		{
 				return "ZERO";
 		}
 		else if (name.equals("08"))
 		{
 				return "EIGHT";
 		}
 		else if (name.equals("07"))
 		{
 				return "SEVEN";
 		}
 		else if (name.equals("04"))
 		{
 				return "FOUR";
 		}
 		else if (name.equals("05"))
 		{
 				return "FIVE";
 		}
 		else if (name.equals("06"))
 		{
 				return "SIX";
 		}
 		else if (name.equals("01"))
 		{
 				return "ONE";
 		}
 		else if (name.equals("02"))
 		{
 				return "TWO";
 		}
 		else if (name.equals("03"))
 		{
 				return "THREE";
 		}
 		else if (name.equals("-62"))
 		{
 				return "DEC";
 		}
 		else if (name.equals("-63"))
 		{
 				return "DSP";
 		}
 		return "";
 	}	
 	
 }	
 	
 
