 package com.tnes;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PPU {
 	private Debugger debugger = Debugger.getInstance();
 	private MMC mmc;
 	private short nameTableMirroring;
 	private short controlReg1, controlReg2, status;
 	private int spriteMemAddr, ppuMemAddr;
 	private short verticalScrollReg, horizontalScrollReg;
 	private short[][] screenBuffer = new short[241][256];
 	private short scanline;
 	private int elapsedCycles;
 	private short backgroundColor;
 	private boolean frameComplete;
 	private boolean logPPUState;
 
 	// Variables used during execution
 	private int tileIndex;
 	private int nameTableAddress;
 	private int patternTableIndex;
 	private short patternTableByte1, patternTableByte2;
 	private int attributeTableIndex;
 	private short attributeTableByte;
 	private int attributeTableSquare;
 	private List<short[]> applicableSprites;
 
 	public PPU(MMC mmc) {
 		this.mmc = mmc;
 
 		controlReg1 = 0;
 		controlReg2 = 0x18;
 		// TODO: Correct init value?
 		status = 0;
 		spriteMemAddr = 0;
 		nameTableMirroring = 0;
 		// TODO: Correct init value?
 		verticalScrollReg = 0;
 		// TODO: Correct init value?
 		horizontalScrollReg = 0;
 		ppuMemAddr = 0;
 
 		for (int i = 0; i < screenBuffer.length; i++) {
 			for (int j = 0; j < screenBuffer[i].length; j++) {
 				screenBuffer[i][j] = 0;
 			}
 		}
 
 		elapsedCycles = 0;
 		scanline = 0;
 		tileIndex = 0;
 
 		logPPUState = false;
 
 		// Palette black
 		backgroundColor = 0x0F;
 
 		// Frame complete flag must be reset from outside this class
 		frameComplete = false;
 
 		addDebugCommands();
 	}
 
 	public MMC getMMC() {
 		return mmc;
 	}
 
 	public void setMMC(MMC mmc) {
 		this.mmc = mmc;
 	}
 
 	public short getNameTableMirroring() {
 		return nameTableMirroring;
 	}
 
 	public void setNameTableMirroring(short nameTableMirroring) {
 		this.nameTableMirroring = nameTableMirroring;
 	}
 
 	public short getControlReg1() {
 		return controlReg1;
 	}
 
 	public void setControlReg1(short controlReg1) {
 		this.controlReg1 = controlReg1;
 	}
 
 	public short getControlReg2() {
 		return controlReg2;
 	}
 
 	public void setControlReg2(short controlReg2) {
 		this.controlReg2 = controlReg2;
 	}
 
 	public short getStatus() {
 		return status;
 	}
 
 	public void setStatus(short status) {
 		this.status = status;
 	}
 
 	public int getSpriteMemAddr() {
 		return spriteMemAddr;
 	}
 
 	public void setSpriteMemAddr(int spriteMemAddr) {
 		this.spriteMemAddr = spriteMemAddr;
 	}
 
 	public int getPPUMemAddr() {
 		return ppuMemAddr;
 	}
 
 	public void setPPUMemAddr(int ppuMemAddr) {
 		this.ppuMemAddr = ppuMemAddr;
 	}
 
 	public short getVerticalScrollReg() {
 		return verticalScrollReg;
 	}
 
 	public void setVerticalScrollReg(short verticalScrollReg) {
 		this.verticalScrollReg = verticalScrollReg;
 	}
 
 	public short getHorizontalScrollReg() {
 		return horizontalScrollReg;
 	}
 
 	public void setHorizontalScrollReg(short horizontalScrollReg) {
 		this.horizontalScrollReg = horizontalScrollReg;
 	}
 
 	public short[][] getScreenBuffer() {
 		return screenBuffer;
 	}
 
 	public short getScanline() {
 		return scanline;
 	}
 
 	public int getElapsedCycles() {
 		return elapsedCycles;
 	}
 
 	public boolean isFrameComplete() {
 		return frameComplete;
 	}
 
 	public void setFrameComplete(boolean frameComplete) {
 		this.frameComplete = frameComplete;
 	}
 
 	/*
 	 * PPU Control Register 1 flags
 	 */
 	public int getNameTableAddress() {
 		int result = 0;
 
 		int nameTable = (controlReg1 & Constants.PPU_CTRL_1_NAME_TABLE_MASK);
 		result = 0x2000 + (0x400 * nameTable);
 
 		return result;
 	}
 
 	public void setNameTableAddress(int value) {
 		controlReg1 &= ~Constants.PPU_CTRL_1_NAME_TABLE_MASK;
 		controlReg1 |= (value & Constants.PPU_CTRL_1_NAME_TABLE_MASK);
 	}
 
 	public boolean isVerticalRWFlagSet() {
 		// Vertical Read: PPU Address increments by 32 on read or write
 		return ((controlReg1 & Constants.PPU_CTRL_1_VERTICAL_WRITE) != 0);
 	}
 
 	public void setVerticalRWFlag(boolean value) {
 		// Vertical Read: PPU Address increments by 32 on read or write
 		if (value)
 			controlReg1 |= Constants.PPU_CTRL_1_VERTICAL_WRITE;
 		else
 			controlReg1 &= ~Constants.PPU_CTRL_1_VERTICAL_WRITE;
 	}
 
 	public boolean isSpritePatternTableAddressFlagSet() {
 		return ((controlReg1 & Constants.PPU_CTRL_1_SPRITE_PATTERN_TABLE_ADDRESS) != 0);
 	}
 
 	public int getSpritePatternTableAddress() {
 		return isSpritePatternTableAddressFlagSet() ? 0x1000 : 0x0000;
 	}
 
 	public void setSpritePatternTableAddressFlag(boolean value) {
 		if (value)
 			controlReg1 |= Constants.PPU_CTRL_1_SPRITE_PATTERN_TABLE_ADDRESS;
 		else
 			controlReg1 &= ~Constants.PPU_CTRL_1_SPRITE_PATTERN_TABLE_ADDRESS;
 	}
 
 	public boolean isScreenPatternTableAddressFlagSet() {
 		return ((controlReg1 & Constants.PPU_CTRL_1_SCREEN_PATTERN_TABLE_ADDRESS) != 0);
 	}
 
 	public int getScreenPatternTableAddress() {
 		return isScreenPatternTableAddressFlagSet() ? 0x1000 : 0x0000;
 	}
 
 	public void setScreenPatternTableAddressFlag(boolean value) {
 		if (value)
 			controlReg1 |= Constants.PPU_CTRL_1_SCREEN_PATTERN_TABLE_ADDRESS;
 		else
 			controlReg1 &= ~Constants.PPU_CTRL_1_SCREEN_PATTERN_TABLE_ADDRESS;
 	}
 
 	public boolean isSpriteSizeFlagSet() {
 		return ((controlReg1 & Constants.PPU_CTRL_1_SPRITE_SIZE) != 0);
 	}
 
 	public void setSpriteSizeFlag(boolean value) {
 		if (value)
 			controlReg1 |= Constants.PPU_CTRL_1_SPRITE_SIZE;
 		else
 			controlReg1 &= ~Constants.PPU_CTRL_1_SPRITE_SIZE;
 	}
 
 	public boolean isVBlankEnableFlagSet() {
 		return ((controlReg1 & Constants.PPU_CTRL_1_VBLANK_ENABLE) != 0);
 	}
 
 	public void setVBlankEnableFlag(boolean value) {
 		if (value)
 			controlReg1 |= Constants.PPU_CTRL_1_VBLANK_ENABLE;
 		else
 			controlReg1 &= ~Constants.PPU_CTRL_1_VBLANK_ENABLE;
 	}
 
 	/*
 	 * PPU Control Register 2 flags
 	 */
 	public boolean isImageMaskFlagSet() {
 		return ((controlReg2 & Constants.PPU_CTRL_2_IMAGE_MASK) != 0);
 	}
 
 	public void setImageMaskFlag(boolean value) {
 		if (value)
 			controlReg2 |= Constants.PPU_CTRL_2_IMAGE_MASK;
 		else
 			controlReg2 &= ~Constants.PPU_CTRL_2_IMAGE_MASK;
 	}
 
 	public boolean isSpriteMaskFlagSet() {
 		return ((controlReg2 & Constants.PPU_CTRL_2_SPRITE_MASK) != 0);
 	}
 
 	public void setSpriteMaskFlag(boolean value) {
 		if (value)
 			controlReg2 |= Constants.PPU_CTRL_2_SPRITE_MASK;
 		else
 			controlReg2 &= ~Constants.PPU_CTRL_2_SPRITE_MASK;
 	}
 
 	public boolean isScreenEnableFlagSet() {
 		return ((controlReg2 & Constants.PPU_CTRL_2_SCREEN_ENABLE) != 0);
 	}
 
 	public void setScreenEnableFlag(boolean value) {
 		if (value)
 			controlReg2 |= Constants.PPU_CTRL_2_SCREEN_ENABLE;
 		else
 			controlReg2 &= ~Constants.PPU_CTRL_2_SCREEN_ENABLE;
 	}
 
 	public boolean isSpriteEnableFlagSet() {
 		return ((controlReg2 & Constants.PPU_CTRL_2_SPRITE_ENABLE) != 0);
 	}
 
 	public void setSpriteEnableFlag(boolean value) {
 		if (value)
 			controlReg2 |= Constants.PPU_CTRL_2_SPRITE_ENABLE;
 		else
 			controlReg2 &= ~Constants.PPU_CTRL_2_SPRITE_ENABLE;
 	}
 
 	public short getBackgroundColorBits() {
 		short result = (short) (controlReg2 >> 5);
 		return result;
 	}
 
 	public void setBackgroundColorBits(short value) {
 		controlReg2 &= ~Constants.PPU_CTRL_2_BKG_COLOR_MASK;
 		controlReg2 |= (value << (short) 5);
 	}
 
 	/*
 	 * PPU Status Register flags
 	 */
 	public boolean isHitFlagSet() {
 		return ((status & Constants.PPU_STAT_HIT) != 0);
 	}
 
 	public void setHitFlag(boolean hitFlag) {
 		if (hitFlag)
 			status |= Constants.PPU_STAT_HIT;
 		else
 			status &= ~Constants.PPU_STAT_HIT;
 	}
 
 	public boolean isVBlankFlagSet() {
 		return ((status & Constants.PPU_STAT_VBLANK) != 0);
 	}
 
 	public void setVBlankFlag(boolean vblankFlag) {
 		if (vblankFlag)
 			status |= Constants.PPU_STAT_VBLANK;
 		else
 			status &= ~Constants.PPU_STAT_VBLANK;
 	}
 
 	/*
 	 * PPU Logic
 	 */
 	public void execute(int cycles) {
 		boolean vblankHit = false;
 		int preVBlankCycles = 0;
 
 		for (int cycle = 1; cycle <= cycles; cycle++) {
 			// What scanline are we on?
 			scanline = (short) ((cycle + elapsedCycles) / Constants.SCANLINE_CYCLES);
 			int scanlineCycle = ((cycle + elapsedCycles) % Constants.SCANLINE_CYCLES);
 			int tilePixel = scanlineCycle % 8;
 
 			if (scanline < 20) {
 				// Do nothing for first 20 scanlines
 			} else if (scanline == 20) {
 				/*
 				 * Scanline 20 has some unique properties, first of all pull
 				 * down the VInt flag
 				 */
 				setVBlankFlag(false);
 
 				if (tilePixel == 0) {
 					/*
 					 * Read name table byte, attribute byte and 2 pattern table
 					 * bytes
 					 */
 					int trueScanline = scanline - 20;
 					tileIndex = PPUHelper.getTileIndex(trueScanline, scanlineCycle);
 					nameTableAddress = getNameTableAddress();
 					// From Name Table
 					patternTableIndex = mmc.readPPUMem(nameTableAddress + tileIndex);
 					patternTableByte1 = mmc.readPPUMem(getScreenPatternTableAddress() + PPUHelper.getPatternTableByte1Index(patternTableIndex, trueScanline));
 					patternTableByte2 = mmc.readPPUMem(getScreenPatternTableAddress() + PPUHelper.getPatternTableByte2Index(patternTableIndex, trueScanline));
 					attributeTableIndex = PPUHelper.getAttributeTableIndex(tileIndex);
 					attributeTableByte = mmc.readPPUMem(nameTableAddress + Constants.NAME_TABLE_SIZE + attributeTableIndex);
 				}
 
 				// Nothing actually rendered on this scanline
 			} else if (scanline < 261) {
 				// Ordinary rendering for scanline 21 thru 260
 				int trueScanline = scanline - 20;
 				if (tilePixel == 0) {
 					/*
 					 * Read Name Table byte, Attribute Table byte and 2 Pattern
 					 * Table bytes
 					 */
 					if (isScreenEnableFlagSet()) {
 						tileIndex = PPUHelper.getTileIndex(trueScanline, scanlineCycle);
 						nameTableAddress = getNameTableAddress();
 						// From Name Table
 						patternTableIndex = mmc.readPPUMem(nameTableAddress + tileIndex);
 						patternTableByte1 = mmc.readPPUMem(getScreenPatternTableAddress() + PPUHelper.getPatternTableByte1Index(patternTableIndex, trueScanline));
 						patternTableByte2 = mmc.readPPUMem(getScreenPatternTableAddress() + PPUHelper.getPatternTableByte2Index(patternTableIndex, trueScanline));
 						attributeTableIndex = PPUHelper.getAttributeTableIndex(tileIndex);
 						attributeTableByte = mmc.readPPUMem(nameTableAddress + Constants.NAME_TABLE_SIZE + attributeTableIndex);
 						attributeTableSquare = PPUHelper.getAttributeTableSquare(tileIndex);
 					}
 
 					// Get sprites for this tile
 					if (isSpriteEnableFlagSet())
 						applicableSprites = getApplicableSprites(trueScanline, scanlineCycle, isSpriteSizeFlagSet());
 					else
 						applicableSprites = null;
 				}
 
 				// Draw screen
 				int paletteIndex = 0;
 				if (isScreenEnableFlagSet()) {
					paletteIndex |= ((patternTableByte1 & PPUHelper.PATTERN_TABLE_BIT_MASK[tilePixel]) >> PPUHelper.PATTERN_TABLE_BYTE1_BIT_SHIFT[tilePixel]);
					paletteIndex |= ((patternTableByte2 & PPUHelper.PATTERN_TABLE_BIT_MASK[tilePixel]) >> PPUHelper.PATTERN_TABLE_BYTE2_BIT_SHIFT[tilePixel]);
					paletteIndex |= ((attributeTableByte & PPUHelper.ATTRIBUTE_TABLE_BIT_MASK[attributeTableSquare]) >> PPUHelper.ATTRIBUTE_TABLE_BIT_SHIFT[attributeTableSquare]);
 
 					if (scanlineCycle < 8) {
 						if (!isImageMaskFlagSet()) {
 							// Draw left 8 pixels of screen...
 							screenBuffer[trueScanline][scanlineCycle] = mmc.readPPUMem(Constants.IMAGE_PALETTE_LO + paletteIndex);
 						}
 					} else if (scanlineCycle < 256) {
 						screenBuffer[trueScanline][scanlineCycle] = mmc.readPPUMem(Constants.IMAGE_PALETTE_LO + paletteIndex);
 					}
 				}
 
 				// Draw sprites
 				paletteIndex = 0;
 				if (isSpriteEnableFlagSet()) {
 					if (applicableSprites != null && !applicableSprites.isEmpty()) {
 						/*
 						 * TODO: Just dealing with 1 sprite for now (need to put
 						 * in logic for multiple sprites, collisions, etc) TODO:
 						 * Deal with 8x16 sprites
 						 */
 						short[] spriteBytes = applicableSprites.get(0);
 						if (spriteBytes != null) {
 							short spriteY = (short) (spriteBytes[0] + 1);
 							short spriteFlags = spriteBytes[2];
 							short spriteX = (short) spriteBytes[3];
 							short spritePatternTableIndex = spriteBytes[1];
 							short spritePatternTableByte1 = mmc.readPPUMem(getSpritePatternTableAddress() + PPUHelper.getPatternTableByte1Index(spritePatternTableIndex, trueScanline - spriteY));
 							short spritePatternTableByte2 = mmc.readPPUMem(getSpritePatternTableAddress() + PPUHelper.getPatternTableByte2Index(spritePatternTableIndex, trueScanline - spriteY));
 
 							int spriteXOffset = scanlineCycle - spriteX;
 							int maskAndShiftIndex = (spriteXOffset < 0) ? (8 + spriteXOffset) : spriteXOffset;
							paletteIndex |= ((spritePatternTableByte1 & PPUHelper.PATTERN_TABLE_BIT_MASK[maskAndShiftIndex]) >> PPUHelper.PATTERN_TABLE_BYTE1_BIT_SHIFT[maskAndShiftIndex]);
							paletteIndex |= ((spritePatternTableByte2 & PPUHelper.PATTERN_TABLE_BIT_MASK[maskAndShiftIndex]) >> PPUHelper.PATTERN_TABLE_BYTE2_BIT_SHIFT[maskAndShiftIndex]);
 							paletteIndex |= ((spriteFlags & 0x3) << 2);
 
 							if (scanlineCycle < 8) {
 								if (!isSpriteMaskFlagSet() && paletteIndex != 0) {
 									// Color #0 is transparent
 									screenBuffer[trueScanline][scanlineCycle] = mmc.readPPUMem(Constants.SPRITE_PALETTE_LO + paletteIndex);
 								}
 							} else if (scanlineCycle < 256 && paletteIndex != 0) {
 								// Color #0 is transparent
 								screenBuffer[trueScanline][scanlineCycle] = mmc.readPPUMem(Constants.SPRITE_PALETTE_LO + paletteIndex);
 							}
 						}
 					}
 				}
 
 			} else if (scanline == 261) {
 				// Again, do nothing
 			} else {
 				// Frame complete
 				vblankHit = true;
 				elapsedCycles = 0;
 				preVBlankCycles = cycle;
 				postFrame();
 			}
 		}
 
 		if (vblankHit) {
 			elapsedCycles = cycles - preVBlankCycles;
 		} else {
 			elapsedCycles += cycles;
 		}
 	}
 
 	public void preFrame() {
 		/*
 		 * Fill the screen with the background color
 		 * fillScreenWithBackgroundColor();
 		 */
 
 		// Clear Sprite #0 hit flag
 		setHitFlag(false);
 	}
 
 	public void postFrame() {
 		// Raise the VBlank flag, NMI will be triggered
 		setVBlankFlag(true);
 
 		// debugger.debugPrint("\nVBlank.");
 		if (logPPUState)
 			debugger.debugLog("\nVBlank");
 
 		// Force CPU Non-maskable interrupt
 		if (isVBlankEnableFlagSet())
 			mmc.getCPU().NMI();
 
 		frameComplete = true;
 	}
 
 	public void updateBackgroundColor() {
 		short color = getBackgroundColorBits();
 
 		// Default to Black
 		backgroundColor = 0x0F;
 		switch (color) {
 		case 0:
 			// Palette Black
 			backgroundColor = 0x0F;
 			break;
 		case 1:
 			// Palette Blue
 			backgroundColor = 0x01;
 			break;
 		case 2:
 			// Palette Green
 			backgroundColor = 0x09;
 			break;
 		case 4:
 			// Palette Red
 			backgroundColor = 0x06;
 			break;
 		}
 	}
 
 	public void fillScreenWithBackgroundColor() {
 		for (int i = 0; i < screenBuffer.length; i++) {
 			for (int j = 0; j < screenBuffer[i].length; j++) {
 				screenBuffer[i][j] = backgroundColor;
 			}
 		}
 	}
 
 	/*
 	 * Sprite stuff
 	 */
 	private List<short[]> getApplicableSprites(int scanline, int scanlineCycle, boolean largeSize) {
 		// Note: if 'largeSize' = true, sprites are 8x16, otherwise 8x8
 		List<short[]> result = new ArrayList<short[]>();
 		int height = largeSize ? 16 : 8;
 		int width = 8;
 		int yRangeMin = scanline;
 		int yRangeMax = scanline + height;
 		int xRangeMin = scanlineCycle;
 		int xRangeMax = scanlineCycle + width;
 		for (int i = 0; i < 64; i++) {
 			short[] bytes = new short[4];
 			int spriteIndex = i * 4;
 			bytes[0] = mmc.getSpriteMem()[spriteIndex];
 			bytes[1] = mmc.getSpriteMem()[spriteIndex + 1];
 			bytes[2] = mmc.getSpriteMem()[spriteIndex + 2];
 			bytes[3] = mmc.getSpriteMem()[spriteIndex + 3];
 
 			int y = bytes[0] + 1;
 			int x = bytes[3];
 			if ((y >= yRangeMin && y < yRangeMax) && (x >= xRangeMin && x < xRangeMax)) {
 				result.add(bytes);
 			}
 		}
 
 		return result;
 	}
 
 	private void addDebugCommands() {
 		try {
 			debugger.addCommand("enablePPULogging", PPU.class.getMethod("__enablePPULogging", String.class), this);
 			debugger.addCommand("disablePPULogging", PPU.class.getMethod("__disablePPULogging", String.class), this);
 		} catch (NoSuchMethodException e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	public void __enablePPULogging(String param) {
 		logPPUState = true;
 	}
 
 	public void __disablePPULogging(String param) {
 		logPPUState = false;
 	}
 }
