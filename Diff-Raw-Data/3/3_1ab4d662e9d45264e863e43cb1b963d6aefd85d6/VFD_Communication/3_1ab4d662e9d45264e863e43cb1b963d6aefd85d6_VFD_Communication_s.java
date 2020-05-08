 package jp.yishii.vfd_driver;
 
 import java.io.UnsupportedEncodingException;
 
 /*
  * Noritake ITRON's VFD Command creator class
  * for Android
  * 
  * Tested with Noritake ITRON's GU-256X64-3101
  * 
  * Coded by Yasuhiro ISHII
  * 
  * most of the APIs in this class are compatible with Noritake ITRON's C++ Control class for VC++/CLI.
  */
 
 public class VFD_Communication {
 	private static final String TAG = "VFD_Communication";
 
 	private VFD_Driver mVFD_Driver;
 
 	private static byte VAL_ESC = 0x1b;
 	private static byte VAL_US = 0x1f;
 
 	public VFD_Communication(VFD_Driver vd) {
 		mVFD_Driver = vd;
 	}
 
 	public void initialize() {
 		byte[] cmd = new byte[2];
 		cmd[0] = VAL_ESC;
 		cmd[1] = 0x40;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void brightness(int Percent) {
 		byte[] cmd = new byte[3];
 
 		if (Percent >= 100) {
 			Percent = 100;
 		}
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x58;
 		cmd[2] = (byte) ((Percent * 0x8 / 100) + 0x10);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void cursorSet(int X, int Y) {
 		byte[] cmd = new byte[6];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x24;
 
 		cmd[2] = (byte) (X % 256);
 		cmd[3] = (byte) ((X / 256) % 256);
 		cmd[4] = (byte) (Y % 256);
 		cmd[5] = (byte) ((Y / 256) % 256);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void cursorOn(boolean on) {
 		byte[] cmd = new byte[3];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x43;
 		if (on) {
 			cmd[2] = 0x01;
 		} else {
 			cmd[2] = 0x00;
 		}
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void screenModeSelect(int a) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x77;
 		cmd[3] = 0x10;
 		if (a == 0x00) {
 			cmd[4] = 0x00;
 		} else {
 			cmd[4] = 0x01;
 		}
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void internationalFontSet(int n) {
 		byte[] cmd = new byte[3];
 
 		cmd[0] = VAL_ESC;
 		cmd[1] = 0x52;
 		cmd[2] = (byte) n;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void characterCodeType(int n) {
 		byte[] cmd = new byte[3];
 
 		cmd[0] = VAL_ESC;
 		cmd[1] = 0x74;
 		cmd[2] = (byte) n;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void endOfLineMode_overWrite() {
 		byte[] cmd = new byte[2];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x01;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void endOfLineMode_VScroll() {
 		byte[] cmd = new byte[2];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x02;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void endOfLineMode_HScroll() {
 		byte[] cmd = new byte[2];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x03;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void horizontalScrollSpeed(int n) {
 		byte[] cmd = new byte[3];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x73;
 		cmd[2] = (byte) n;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void fontSize(int m) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x67;
 		cmd[3] = 0x01;
 		cmd[4] = (byte) m;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void specify2ByteCharaMode(boolean m) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x67;
 		cmd[3] = 0x01;
 		if (m) {
 			cmd[4] = 1;
 		} else {
 			cmd[4] = 0;
 		}
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void select2ByteCharType(int m) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x67;
 		cmd[3] = 0x03;
 		cmd[4] = (byte) m;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void fontMagnify(int X, int Y) {
 		byte[] cmd = new byte[6];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x67;
 		cmd[3] = 0x40;
 		cmd[4] = (byte) X;
 		cmd[5] = (byte) Y;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void characterBold(boolean bold) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x67;
 		cmd[3] = 0x41;
 		if (bold) {
 			cmd[4] = 0x01;
 		} else {
 			cmd[4] = 0x00;
 		}
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void wait(byte t_halfSec) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x61;
 		cmd[3] = 0x01;
 		cmd[4] = t_halfSec;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void shortWait(byte t_14mSec) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x61;
 		cmd[3] = 0x02;
 		cmd[4] = t_14mSec;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void scrollAction(int width, int count, int speed) {
 		byte[] cmd = new byte[9];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x61;
 		cmd[3] = 0x10;
 		cmd[4] = (byte) (width % 256);
 		cmd[5] = (byte) ((width / 256) % 256);
 		cmd[6] = (byte) (count % 256);
 		cmd[7] = (byte) ((count / 256) % 256);
 		cmd[8] = (byte) speed;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void displayBlinkAction(byte pattern, byte onTime, byte offtime,
 			byte numberOfRepeat) {
 		byte[] cmd = new byte[8];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x61;
 		cmd[3] = 0x11;
 		cmd[4] = pattern;
 		cmd[5] = onTime;
 		cmd[6] = offtime;
 		cmd[7] = numberOfRepeat;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void curtainAction(int direction, int speed, int pattern) {
 		byte[] cmd = new byte[7];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x61;
 		cmd[3] = 0x12;
 		cmd[4] = (byte) direction;
 		cmd[5] = (byte) speed;
 		cmd[6] = (byte) pattern;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void springAction(int direction, int speed, int patternAddress) {
 		byte[] cmd = new byte[8];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x61;
 		cmd[3] = 0x13;
 		cmd[4] = (byte) direction;
 		cmd[5] = (byte) speed;
 		cmd[6] = (byte) (patternAddress % 256);
 		cmd[7] = (byte) ((patternAddress / 256) % 256);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void randomAction(int speed, int patternAddress) {
 		byte[] cmd = new byte[7];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x61;
 		cmd[3] = 0x14;
 		cmd[4] = (byte) speed;
 		cmd[5] = (byte) (patternAddress % 256);
 		cmd[6] = (byte) ((patternAddress / 256) % 256);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void displayPowerOn(boolean on) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x61;
 		cmd[3] = 0x40;
 		if (on) {
 			cmd[4] = 0x01;
 		} else {
 			cmd[4] = 0x00;
 		}
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void dotPatternDraw(boolean pen, int X, int Y) {
 		byte[] cmd = new byte[9];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x64;
 		cmd[3] = 0x10;
 		if (pen) {
 			cmd[4] = 0x01;
 		} else {
 			cmd[4] = 0x00;
 		}
 		cmd[5] = (byte) (X % 256);
 		cmd[6] = (byte) ((X / 256) % 256);
 		cmd[7] = (byte) (Y % 256);
 		cmd[8] = (byte) ((Y / 256) % 256);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void lineBoxDraw(int mode, boolean pen, int X1, int Y1, int X2,
 			int Y2) {
 		byte[] cmd = new byte[14];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x64;
 		cmd[3] = 0x11;
 		cmd[4] = (byte) (mode % 256);
 		if (pen) {
 			cmd[5] = 0x01;
 		} else {
 			cmd[5] = 0x00;
 		}
 		cmd[6] = (byte) (X1 % 256);
 		cmd[7] = (byte) ((X1 / 256) % 256);
 		cmd[8] = (byte) (Y1 % 256);
 		cmd[9] = (byte) ((Y1 / 256) % 256);
 
 		cmd[10] = (byte) (X2 % 256);
 		cmd[11] = (byte) ((X2 / 256) % 256);
 		cmd[12] = (byte) (Y2 % 256);
 		cmd[13] = (byte) ((Y2 / 256) % 256);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void realTimeBitImageDisplay(int X, int Y, byte[] d) {
 		if (d.length < (X * Y)) {
 			// this action is same as NoritakeITRON's C++ Sample
 			this.print("*Error*");
 		} else {
 			byte[] cmd = new byte[X * Y + 9];
 
 			cmd[0] = VAL_US;
 			cmd[1] = 0x28;
 			cmd[2] = 0x66;
 			cmd[3] = 0x11;
 			cmd[4] = (byte) (X % 256);
 			cmd[5] = (byte) ((X / 256) % 256);
 			cmd[6] = (byte) (Y % 256);
 			cmd[7] = (byte) ((Y / 256) % 256);
			System.arraycopy(d, 0, cmd, 8, X * Y);
 
 			mVFD_Driver.write(cmd);
 		}
 	}
 
 	public void ramBitImageDefinition(int address, int size, byte[] d) {
 		byte[] cmd = new byte[d.length + 10];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x66;
 		cmd[3] = 0x01;
 		cmd[4] = (byte) (address % 0x100);
 		cmd[5] = (byte) ((address / 0x100) % 0x100);
 		cmd[6] = (byte) ((address / 0x10000) % 0x100);
 		cmd[7] = (byte) (size % 0x100);
 		cmd[8] = (byte) ((size / 0x100) % 0x100);
 		cmd[9] = (byte) ((size / 0x10000) % 0x100);
 		System.arraycopy(d, 0, cmd, 10, d.length);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void FromBitImageDefinition(int address, int size, byte[] d) {
 		byte[] cmd = new byte[d.length + 10];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x65;
 		cmd[3] = 0x10;
 		cmd[4] = (byte) (address % 0x100);
 		cmd[5] = (byte) ((address / 0x100) % 0x100);
 		cmd[6] = (byte) ((address / 0x10000) % 0x100);
 		cmd[7] = (byte) (size % 0x100);
 		cmd[8] = (byte) ((size / 0x100) % 0x100);
 		cmd[9] = (byte) ((size / 0x10000) % 0x100);
 		System.arraycopy(d, 0, cmd, 10, d.length);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void displayDownloadedImage(int memory, int address, int imageSizeY,
 			int displaySizeX, int displaySizeY) {
 		byte[] cmd = new byte[15];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x66;
 		cmd[3] = 0x10;
 		cmd[4] = (byte) memory;
 		cmd[5] = (byte) (address % 0x100);
 		cmd[6] = (byte) ((address / 0x100) % 0x100);
 		cmd[7] = (byte) ((address / 0x10000) % 0x100);
 		cmd[8] = (byte) (imageSizeY % 256);
 		cmd[9] = (byte) ((imageSizeY / 256) % 256);
 		cmd[10] = (byte) (displaySizeX % 256);
 		cmd[11] = (byte) ((displaySizeX / 256) % 256);
 		cmd[12] = (byte) (displaySizeY % 256);
 		cmd[13] = (byte) ((displaySizeY / 256) % 256);
 		cmd[14] = 0x01;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void displayDownloadedImage_scroll(int memory, int address,
 			int imageSizeY, int displaySizeX, int displaySizeY, int speed) {
 		byte[] cmd = new byte[16];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x66;
 		cmd[3] = (byte) 0x90;
 		cmd[4] = (byte) memory;
 		cmd[5] = (byte) (address % 0x100);
 		cmd[6] = (byte) ((address / 0x100) % 0x100);
 		cmd[7] = (byte) ((address / 0x10000) % 0x100);
 		cmd[8] = (byte) (imageSizeY % 256);
 		cmd[9] = (byte) ((imageSizeY / 256) % 256);
 		cmd[10] = (byte) (displaySizeX % 256);
 		cmd[11] = (byte) ((displaySizeX / 256) % 256);
 		cmd[12] = (byte) (displaySizeY % 256);
 		cmd[13] = (byte) ((displaySizeY / 256) % 256);
 		cmd[14] = 0x01;
 		cmd[15] = (byte) speed;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void horizontalScrollQuality(boolean onSpeed) {
 		byte[] cmd = new byte[3];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x6d;
 		if (onSpeed) {
 			cmd[2] = 0x00;
 		} else {
 			cmd[2] = 0x01;
 		}
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void specifyReverseDisplay(boolean reverse) {
 		byte[] cmd = new byte[3];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x72;
 		if (reverse) {
 			cmd[2] = 0x00;
 		} else {
 			cmd[2] = 0x01;
 		}
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void specifyWriteMixMode_none() {
 		byte[] cmd = new byte[3];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x77;
 		cmd[2] = 0x00;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void specifyWriteMixMode_or() {
 		byte[] cmd = new byte[3];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x77;
 		cmd[2] = 0x01;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void specifyWriteMixMode_and() {
 		byte[] cmd = new byte[3];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x77;
 		cmd[2] = 0x02;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void specifyWriteMixMode_exOr() {
 		byte[] cmd = new byte[3];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x77;
 		cmd[2] = 0x03;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void specifyWriteMixMode_exOr(int a) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x77;
 		cmd[3] = 0x01;
 		cmd[4] = (byte) (a % 0x100);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void userWindowDinition(int number, boolean define, int left,
 			int top, int width, int height) {
 		byte[] cmd;
 
 		if (define == false) {
 			cmd = new byte[16]; // wrong size?
 
 			cmd[0] = VAL_US;
 			cmd[1] = 0x28;
 			cmd[2] = 0x77;
 			cmd[3] = 0x02;
 			cmd[4] = (byte) number;
 			cmd[5] = 0x00;
 
 		} else {
 			cmd = new byte[14];
 
 			cmd[0] = VAL_US;
 			cmd[1] = 0x28;
 			cmd[2] = 0x77;
 			cmd[3] = 0x02;
 			cmd[4] = (byte) number;
 			cmd[5] = 0x01;
 			cmd[6] = (byte) (left % 256);
 			cmd[7] = (byte) ((left / 256) % 256);
 			cmd[8] = (byte) (top % 256);
 			cmd[9] = (byte) ((top / 256) % 256);
 			cmd[10] = (byte) (width % 256);
 			cmd[11] = (byte) ((width / 256) % 256);
 			cmd[12] = (byte) (height % 256);
 			cmd[13] = (byte) ((height / 256) % 256);
 		}
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void userWindowDinition(int number, boolean define) {
 		byte[] cmd = new byte[6];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x77;
 		cmd[3] = 0x02;
 		cmd[4] = (byte) number;
 		cmd[5] = 0x00;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void downloadCharacterSpecify(boolean enable) {
 		byte[] cmd = new byte[3];
 
 		cmd[0] = VAL_ESC;
 		cmd[1] = 0x25;
 		if (enable) {
 			cmd[2] = 0x01;
 		} else {
 			cmd[2] = 0x00;
 		}
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void downloadCharacterDifinition(int characterType,
 			byte codeBeginWith, byte codeEndWith, byte[] d) {
 		byte[] cmd = new byte[d.length + 5];
 
 		cmd[0] = VAL_ESC;
 		cmd[1] = 0x26;
 		cmd[2] = (byte) characterType;
 		cmd[3] = codeBeginWith;
 		cmd[4] = codeEndWith;
 		System.arraycopy(d, 0, cmd, 5, d.length);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void downloadedCharacterDelete(int characterType, byte code) {
 		byte[] cmd = new byte[4];
 
 		cmd[0] = VAL_ESC;
 		cmd[1] = 0x3f;
 		cmd[2] = (byte) characterType;
 		cmd[3] = code;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void downloadCharaDefinition_16x16dot(int code, byte[] d) {
 		byte[] cmd = new byte[d.length + 6];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x67;
 		cmd[3] = 0x10;
 		cmd[4] = (byte) ((code / 256) % 256);
 		cmd[5] = (byte) (code % 256);
 		System.arraycopy(d, 0, cmd, 6, d.length);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void downloadCharacterDelete_16x16dot(byte code) {
 		byte[] cmd = new byte[6];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x67;
 		cmd[3] = 0x11;
 		cmd[4] = (byte) ((code / 256) % 256);
 		cmd[5] = (byte) (code % 256);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void downloadedCharaSaveFromRamToFROM(int FontType) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x65;
 		cmd[3] = 0x11;
 		cmd[4] = (byte) FontType;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void downloadedCharaTranFromFROMToRam(int FontType) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x65;
 		cmd[3] = 0x21;
 		cmd[4] = (byte) FontType;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void downloadFromUserFontDifinition(int fontType, byte[] pattern) {
 		byte[] cmd = new byte[pattern.length + 5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x65;
 		cmd[3] = 0x13;
 		cmd[4] = (byte) fontType;
 		System.arraycopy(pattern, 0, cmd, 5, pattern.length);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void userSetupModeStart() {
 		byte[] cmd = new byte[6];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x65;
 		cmd[3] = 0x01;
 		cmd[4] = 'I';
 		cmd[5] = 'N';
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void userSetupModeEnd() {
 		byte[] cmd = new byte[7];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x65;
 		cmd[3] = 0x02;
 		cmd[4] = 'O';
 		cmd[5] = 'U';
 		cmd[6] = 'T';
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void IOPortSetting(int portNumber, byte output) {
 		byte[] cmd = new byte[6];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x70;
 		cmd[3] = 0x01;
 		cmd[4] = (byte) portNumber;
 		cmd[5] = output;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void IOPortOut(int portNumber, byte pattern) {
 		byte[] cmd = new byte[6];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x70;
 		cmd[3] = 0x10;
 		cmd[4] = (byte) portNumber;
 		cmd[5] = pattern;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	// need modify to retrieve IN packet,yishii
 	public void IOPortInput(int portNumber) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x70;
 		cmd[3] = 0x20;
 		cmd[4] = (byte) portNumber;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void macroRamMacroDifiition(byte[] macroCode) {
 		byte[] cmd = new byte[macroCode.length + 4];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x3a;
 		cmd[2] = (byte) (macroCode.length % 256);
 		cmd[3] = (byte) ((macroCode.length / 256) % 256);
 		System.arraycopy(macroCode, 0, cmd, 4, macroCode.length);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void macroFromMacroDifiition(int macroNumber, byte t1, byte t2,
 			byte[] macroCode) {
 		byte[] cmd = new byte[macroCode.length + 9];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x65;
 		cmd[3] = 0x12;
 		cmd[4] = (byte) macroNumber;
 		cmd[5] = (byte) (macroCode.length % 256);
 		cmd[6] = (byte) ((macroCode.length / 256) % 256);
 		cmd[7] = t1;
 		cmd[8] = t2;
 
 		System.arraycopy(macroCode, 0, cmd, 9, macroCode.length);
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void macroExecution(int macroNumberAssigned, byte t1, byte t2) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x5e;
 		cmd[2] = (byte) macroNumberAssigned;
 		cmd[3] = t1;
 		cmd[4] = t2;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void memorySwSet(int switchNumber, int content) {
 		byte[] cmd = new byte[6];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x65;
 		cmd[3] = 0x03;
 		cmd[4] = (byte) switchNumber;
 		cmd[5] = (byte) content;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	// need modify to read value,yishii
 	public void memorySwRead(int switchNumber) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x65;
 		cmd[3] = 0x04;
 		cmd[4] = (byte) switchNumber;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	// need modify to read value,yishii
 	public void displayStatusRead(int typeOfInfo, int startAddress,
 			int dataLength) {
 		byte[] cmd = new byte[7];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x65;
 		cmd[3] = 0x40;
 		cmd[4] = (byte) typeOfInfo;
 		cmd[5] = (byte) startAddress;
 		cmd[6] = (byte) dataLength;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	// need modify to read value,yishii
 	public void displayStatusRead(int typeOfInfo) {
 		byte[] cmd = new byte[5];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x65;
 		cmd[3] = 0x40;
 		cmd[4] = (byte) typeOfInfo;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void setupAsianFont_Japanese() {
 		byte[] cmd = new byte[15];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x67;
 		cmd[3] = 0x01;
 		cmd[4] = 0x02;
 		cmd[5] = 0x1f;
 		cmd[6] = 0x28;
 		cmd[7] = 0x67;
 		cmd[8] = 0x02;
 		cmd[9] = 0x01;
 		cmd[10] = 0x1f;
 		cmd[11] = 0x28;
 		cmd[12] = 0x67;
 		cmd[13] = 0x03;
 		cmd[14] = 0x00;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void setupAsianFont_Korean() {
 		byte[] cmd = new byte[15];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x67;
 		cmd[3] = 0x01;
 		cmd[4] = 0x02;
 		cmd[5] = 0x1f;
 		cmd[6] = 0x28;
 		cmd[7] = 0x67;
 		cmd[8] = 0x02;
 		cmd[9] = 0x01;
 		cmd[10] = 0x1f;
 		cmd[11] = 0x28;
 		cmd[12] = 0x67;
 		cmd[13] = 0x03;
 		cmd[14] = 0x01;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void setupAsianFont_TradChinese() {
 		byte[] cmd = new byte[15];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x67;
 		cmd[3] = 0x01;
 		cmd[4] = 0x02;
 		cmd[5] = 0x1f;
 		cmd[6] = 0x28;
 		cmd[7] = 0x67;
 		cmd[8] = 0x02;
 		cmd[9] = 0x01;
 		cmd[10] = 0x1f;
 		cmd[11] = 0x28;
 		cmd[12] = 0x67;
 		cmd[13] = 0x03;
 		cmd[14] = 0x03;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	public void setupAsianFont_SimplChinese() {
 		byte[] cmd = new byte[15];
 
 		cmd[0] = VAL_US;
 		cmd[1] = 0x28;
 		cmd[2] = 0x67;
 		cmd[3] = 0x01;
 		cmd[4] = 0x02;
 		cmd[5] = 0x1f;
 		cmd[6] = 0x28;
 		cmd[7] = 0x67;
 		cmd[8] = 0x02;
 		cmd[9] = 0x01;
 		cmd[10] = 0x1f;
 		cmd[11] = 0x28;
 		cmd[12] = 0x67;
 		cmd[13] = 0x03;
 		cmd[14] = 0x02;
 
 		mVFD_Driver.write(cmd);
 	}
 
 	/*
 	 * 
 	 */
 
 	public void open() {
 		mVFD_Driver.Open();
 	}
 
 	public void close() {
 		mVFD_Driver.Close();
 	}
 
 	public void print(String str) {
 		mVFD_Driver.write(str.getBytes());
 	}
 
 	public void printJapanese(String str) {
 		try {
 			mVFD_Driver.write(str.getBytes("SJIS"));
 		} catch (UnsupportedEncodingException e) {
 		}
 	}
 
 }
