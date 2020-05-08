 package uk.ac.york.jbb.integratedcircuits;
 
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import uk.ac.york.jbb.jbreadboard.ChipAccess;
 import uk.ac.york.jbb.jbreadboard.v1_00.ChipModel;
 
 public abstract class IntegratedCircuit implements ChipModel {
 	protected ChipAccess chip;
 	protected String name = this.getClass().getSimpleName();
 	protected String description;
 	protected String manufacturer;
 	protected String diagram;
 	protected boolean wide = false;
 
 	protected List<Pin> pins = new ArrayList<Pin>();
 
 	@Override
 	public void setAccess(ChipAccess chip) {
 		this.chip = chip;
 	}
 
 	@Override
 	public abstract void reset();
 
 	@Override
 	public abstract void simulate();
 
 	@Override
 	public String getChipText() {
 		return this.name;
 	}
 
 	@Override
 	public String getDescription() {
 		return this.description;
 	}
 
 	@Override
 	public String getManufacturer() {
 		return this.manufacturer;
 	}
 
 	public int getNumberOfPackagePins() {
 		return this.pins.size();
 	}
 
 	@Override
 	public String getDiagram() {
 		return this.diagram;
 	}
 
 	@Override
 	public boolean isWide() {
 		return this.wide;
 	}
 
 	protected Pin getPin(int pinNumber) throws InvalidPinException {
 		for (Pin pin : this.pins) {
 			if (pin.getPinNumber() == pinNumber) {
 				return pin;
 
 			}
 		}
 		throw new InvalidPinException("OPPS: Pin number does not exist");
 	}
 
 	protected Pin getPin(String pinName) throws InvalidPinException {
 		for (Pin pin : this.pins) {
 			if (pin.getPinName().equals(pinName)) {
 				return pin;
 
 			}
 		}
 		throw new InvalidPinException("OPPS: Pin name does not exist");
 	}
 
 	protected Pin[] getPins(String pinName) throws InvalidPinException {
 		Vector<Pin> match = new Vector<Pin>();
 		for (Pin pin : this.pins) {
 			if (pin.getPinName().equals(pinName)) {
 				match.add(pin);
 
 			}
 		}
 		Pin[] matchedPins = new Pin[match.size()];
 		for (int i = 0; i < matchedPins.length; i++) {
 			matchedPins[i] = (Pin) match.get(i);
 		}
 		return matchedPins;
 	}
 
 	public String getPinName(int pinNumber) throws InvalidPinException {
 		for (Pin pin : this.pins) {
 			if (pin.getPinNumber() == pinNumber) {
 				return pin.getPinName();
 
 			}
 		}
 		throw new InvalidPinException("OPPS: Pin number does not exist");
 	}
 
 	public int getPinNumber(String pinName) throws InvalidPinException {
 		for (Pin pin : this.pins) {
 			if (pin.getPinName().equals(pinName)) {
 				return pin.getPinNumber();
 
 			}
 		}
 		throw new InvalidPinException("OPPS: Pin name does not exist");
 	}
 
 	public boolean isPinInput(int pinNumber) throws InvalidPinException {
 		Pin pin = this.getPin(pinNumber);
 		if (pin instanceof InputPin) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isPinInput(String pinName) throws InvalidPinException {
 		Pin pin = this.getPin(pinName);
 		if (pin instanceof InputPin) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isPinOutput(int pinNumber) throws InvalidPinException {
 		Pin pin = this.getPin(pinNumber);
 		if (pin instanceof OutputPin) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isPinOutput(String pinName) throws InvalidPinException {
 		Pin pin = this.getPin(pinName);
 		if (pin instanceof OutputPin) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isPinInputOutput(int pinNumber) throws InvalidPinException {
 		Pin pin = this.getPin(pinNumber);
 		if (pin instanceof InputOutputPin) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isPinInputOutput(String pinName) throws InvalidPinException {
 		Pin pin = this.getPin(pinName);
 		if (pin instanceof InputOutputPin) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isPinPower(int pinNumber) throws InvalidPinException {
 		Pin pin = this.getPin(pinNumber);
 		if (pin instanceof PowerPin) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isPinPower(String pinName) throws InvalidPinException {
 		Pin pin = this.getPin(pinName);
 		if (pin instanceof PowerPin) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isPinNotConnected(int pinNumber) throws InvalidPinException {
 		Pin pin = this.getPin(pinNumber);
 		if (pin instanceof NotConnectedPin) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isPinNotConnected(String pinName) throws InvalidPinException {
 		Pin pin = this.getPin(pinName);
 		if (pin instanceof NotConnectedPin) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isPinOpenCollector(int pinNumber) throws InvalidPinException {
 		Pin pin = this.getPin(pinNumber);
 		if (pin instanceof OutputPin) {
 			if (((OutputPin) pin).getPinDriver() == Pin.PinDriver.OPEN_COLLECTOR) {
 				return true;
 			}
 			return false;
 		}
 
 		if (pin instanceof InputOutputPin) {
 			if (((InputOutputPin) pin).getPinDriver() == Pin.PinDriver.OPEN_COLLECTOR) {
 				return true;
 			}
 			return false;
 		}
 
 		return false;
 	}
 
 	public boolean isPinOpenCollector(String pinName) throws InvalidPinException {
 		Pin pin = this.getPin(pinName);
 		if (pin instanceof OutputPin) {
 			if (((OutputPin) pin).getPinDriver() == Pin.PinDriver.OPEN_COLLECTOR) {
 				return true;
 			}
 			return false;
 		}
 
 		if (pin instanceof InputOutputPin) {
 			if (((InputOutputPin) pin).getPinDriver() == Pin.PinDriver.OPEN_COLLECTOR) {
 				return true;
 			}
 			return false;
 		}
 
 		return false;
 	}
 
 	public boolean isPinTotemPole(int pinNumber) throws InvalidPinException {
 		Pin pin = this.getPin(pinNumber);
 		if (pin instanceof OutputPin) {
 			if (((OutputPin) pin).getPinDriver() == Pin.PinDriver.TOTEM_POLE) {
 				return true;
 			}
 			return false;
 		}
 
 		if (pin instanceof InputOutputPin) {
 			if (((InputOutputPin) pin).getPinDriver() == Pin.PinDriver.TOTEM_POLE) {
 				return true;
 			}
 			return false;
 		}
 
 		return false;
 	}
 
 	public boolean isPinTotemPole(String pinName) throws InvalidPinException {
 		Pin pin = this.getPin(pinName);
 		if (pin instanceof OutputPin) {
 			if (((OutputPin) pin).getPinDriver() == Pin.PinDriver.TOTEM_POLE) {
 				return true;
 			}
 			return false;
 		}
 
 		if (pin instanceof InputOutputPin) {
 			if (((InputOutputPin) pin).getPinDriver() == Pin.PinDriver.TOTEM_POLE) {
 				return true;
 			}
 			return false;
 		}
 
 		return false;
 	}
 
 	public boolean isPinTriState(int pinNumber) throws InvalidPinException {
 		Pin pin = this.getPin(pinNumber);
 		if (pin instanceof OutputPin) {
 			if (((OutputPin) pin).getPinDriver() == Pin.PinDriver.TRI_STATE) {
 				return true;
 			}
 			return false;
 		}
 
 		if (pin instanceof InputOutputPin) {
 			if (((InputOutputPin) pin).getPinDriver() == Pin.PinDriver.TRI_STATE) {
 				return true;
 			}
 			return false;
 		}
 
 		return false;
 	}
 
 	public boolean isPinTriState(String pinName) throws InvalidPinException {
 		Pin pin = this.getPin(pinName);
 		if (pin instanceof OutputPin) {
 			if (((OutputPin) pin).getPinDriver() == Pin.PinDriver.TRI_STATE) {
 				return true;
 			}
 			return false;
 		}
 
 		if (pin instanceof InputOutputPin) {
 			if (((InputOutputPin) pin).getPinDriver() == Pin.PinDriver.TRI_STATE) {
 				return true;
 			}
 			return false;
 		}
 
 		return false;
 	}
 
 	public boolean isPinDriven(int pinNumber) throws InvalidPinException {
 		Pin pin = this.getPin(pinNumber);
 		if (pin instanceof OutputPin || pin instanceof InputOutputPin || pin instanceof ClockOutputPin) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isPinDriven(String pinName) throws InvalidPinException {
 		Pin pin = this.getPin(pinName);
 		if (pin instanceof OutputPin || pin instanceof InputOutputPin || pin instanceof ClockOutputPin) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isPinClockOutput(int pinNumber) throws InvalidPinException {
 		Pin pin = this.getPin(pinNumber);
 		if (pin instanceof ClockOutputPin) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isPinClockOutput(String pinName) throws InvalidPinException {
 		Pin pin = this.getPin(pinName);
 		if (pin instanceof ClockOutputPin) {
 			return true;
 		}
 		return false;
 	}
 
 	public int getPinIndex(int pinNumber) throws InvalidPinException {
 		for (int i = 0; i < this.pins.size(); i++) {
 			if (this.pins.get(i).getPinNumber() == pinNumber) {
 				return i;
 
 			}
 		}
 		throw new InvalidPinException("OPPS: Pin number does not exist");
 	}
 
 	public int getPinIndex(String pinName) throws InvalidPinException {
 		for (int i = 0; i < this.pins.size(); i++) {
 			if (this.pins.get(i).getPinName().equals(pinName)) {
 				return i;
 
 			}
 		}
 		throw new InvalidPinException("OPPS: Pin name does not exist");
 	}
 
 	public String getPinIndexName(int pinIndex) throws InvalidPinException {
 		if (pinIndex >= 0 && pinIndex < this.pins.size()) {
 			return this.pins.get(pinIndex).getPinName();
 		}
 		throw new InvalidPinException("OPPS: Pin index does not exist");
 	}
 
 	public int getPinIndexNumber(int pinIndex) throws InvalidPinException {
 		if (pinIndex >= 0 && pinIndex < this.pins.size()) {
 			return this.pins.get(pinIndex).getPinNumber();
 		}
 		throw new InvalidPinException("OPPS: Pin index does not exist");
 	}
 
 	public boolean isPinIndexInput(int pinIndex) throws InvalidPinException {
 		if (pinIndex >= 0 && pinIndex < this.pins.size()) {
 			Pin pin = this.pins.get(pinIndex);
 			if (pin instanceof InputPin) {
 				return true;
 			}
 			return false;
 		}
 		throw new InvalidPinException("OPPS: isPinIndexInput( int pinIndex " + pinIndex + " )");
 	}
 
 	public boolean isPinIndexOutput(int pinIndex) throws InvalidPinException {
 		if (pinIndex >= 0 && pinIndex < this.pins.size()) {
 			Pin pin = this.pins.get(pinIndex);
 			if (pin instanceof OutputPin) {
 				return true;
 			}
 			return false;
 		}
 		throw new InvalidPinException("OPPS: isPinIndexOutput( int pinIndex " + pinIndex + " )");
 	}
 
 	public boolean isPinIndexInputOutput(int pinIndex) throws InvalidPinException {
 		if (pinIndex >= 0 && pinIndex < this.pins.size()) {
 			Pin pin = this.pins.get(pinIndex);
 			if (pin instanceof InputOutputPin) {
 				return true;
 			}
 			return false;
 		}
 		throw new InvalidPinException("OPPS: isPinIndexInputOutput( int pinIndex " + pinIndex + " )");
 	}
 
 	public boolean isPinIndexPower(int pinIndex) throws InvalidPinException {
 		if (pinIndex >= 0 && pinIndex < this.pins.size()) {
 			Pin pin = this.pins.get(pinIndex);
 			if (pin instanceof PowerPin) {
 				return true;
 			}
 			return false;
 		}
 		throw new InvalidPinException("OPPS: isPinIndexPower( int pinIndex " + pinIndex + " )");
 	}
 
 	public boolean isPinIndexNotConnected(int pinIndex) throws InvalidPinException {
 		if (pinIndex >= 0 && pinIndex < this.pins.size()) {
 			Pin pin = this.pins.get(pinIndex);
 			if (pin instanceof NotConnectedPin) {
 				return true;
 			}
 			return false;
 		}
 		throw new InvalidPinException("OPPS: isPinIndexNotConnected( int pinIndex " + pinIndex + " )");
 	}
 
 	public boolean isPinIndexOpenCollector(int pinIndex) throws InvalidPinException {
 		if (pinIndex >= 0 && pinIndex < this.pins.size()) {
 			Pin pin = this.pins.get(pinIndex);
 
 			if (pin instanceof OutputPin) {
 				if (((OutputPin) pin).getPinDriver() == Pin.PinDriver.OPEN_COLLECTOR) {
 					return true;
 				}
 				return false;
 			}
 
 			if (pin instanceof InputOutputPin) {
 				if (((InputOutputPin) pin).getPinDriver() == Pin.PinDriver.OPEN_COLLECTOR) {
 					return true;
 				}
 				return false;
 			}
 
 			return false;
 		}
 
 		throw new InvalidPinException("OPPS: isPinIndexOpenCollector( int pinIndex " + pinIndex + " )");
 	}
 
 	public boolean isPinIndexTotemPole(int pinIndex) throws InvalidPinException {
 		if (pinIndex >= 0 && pinIndex < this.pins.size()) {
 			Pin pin = this.pins.get(pinIndex);
 
 			if (pin instanceof OutputPin) {
 				if (((OutputPin) pin).getPinDriver() == Pin.PinDriver.TOTEM_POLE) {
 					return true;
 				}
 				return false;
 			}
 
 			if (pin instanceof InputOutputPin) {
 				if (((InputOutputPin) pin).getPinDriver() == Pin.PinDriver.TOTEM_POLE) {
 					return true;
 				}
 				return false;
 			}
 
 			return false;
 		}
 
 		throw new InvalidPinException("OPPS: isPinIndexTotemPole( int pinIndex " + pinIndex + " )");
 	}
 
 	public boolean isPinIndexTriState(int pinIndex) throws InvalidPinException {
 		if (pinIndex >= 0 && pinIndex < this.pins.size()) {
 			Pin pin = this.pins.get(pinIndex);
 
 			if (pin instanceof OutputPin) {
 				if (((OutputPin) pin).getPinDriver() == Pin.PinDriver.TRI_STATE) {
 					return true;
 				}
 				return false;
 			}
 
 			if (pin instanceof InputOutputPin) {
 				if (((InputOutputPin) pin).getPinDriver() == Pin.PinDriver.TRI_STATE) {
 					return true;
 				}
 				return false;
 			}
 
 			return false;
 		}
 
 		throw new InvalidPinException("OPPS: isPinIndexTriState( int pinIndex " + pinIndex + " )");
 	}
 
 	public boolean isPinIndexDriven(int pinIndex) throws InvalidPinException {
 		if (pinIndex >= 0 && pinIndex < this.pins.size()) {
 			Pin pin = this.pins.get(pinIndex);
 
 			if (pin instanceof OutputPin || pin instanceof InputOutputPin || pin instanceof ClockOutputPin) {
 				return true;
 			}
 			return false;
 		}
 		throw new InvalidPinException("OPPS: isPinIndexDriven( int pinIndex " + pinIndex + " )");
 	}
 
 	@Override
 	public int getNumberOfPins() {
 		return this.pins.size() / 2;
 	}
 
 	public int getPinOffset(String pinName) throws InvalidPinException {
 		return this.getPinNumber(pinName) - 1;
 	}
 
 	public int[] getPinOffsets(String pinName) throws InvalidPinException {
 		Pin[] pins = this.getPins(pinName);
 		int[] pinOffsets = new int[pins.length];
 
 		for (int i = 0; i < pins.length; i++) {
 			if (pins[i].getPinNumber() - 1 < 0) {
 				throw new InvalidPinException("OPPS: getPinOffsets( String pinName " + i + " )");
 			}
 			pinOffsets[i] = pins[i].getPinNumber() - 1;
 		}
 
 		return pinOffsets;
 	}
 
 	public boolean isPinOffsetInput(int pinOffset) throws InvalidPinException {
 		return this.isPinInput(pinOffset + 1);
 	}
 
 	public boolean isPinOffsetOutput(int pinOffset) throws InvalidPinException {
 		return this.isPinOutput(pinOffset + 1);
 	}
 
 	public boolean isPinOffsetInputOutput(int pinOffset) throws InvalidPinException {
 		return this.isPinInputOutput(pinOffset + 1);
 	}
 
 	public boolean isPinOffsetPower(int pinOffset) throws InvalidPinException {
 		return this.isPinPower(pinOffset + 1);
 	}
 
 	public boolean isPinOffsetNotConnected(int pinOffset) throws InvalidPinException {
 		return this.isPinNotConnected(pinOffset + 1);
 	}
 
 	public boolean isPinOffsetOpenCollector(int pinOffset) throws InvalidPinException {
 		return this.isPinOpenCollector(pinOffset + 1);
 	}
 
 	public boolean isPinOffsetClockOutput(int pinOffset) throws InvalidPinException {
 		return this.isPinClockOutput(pinOffset + 1);
 	}
 
 	public boolean isPinOffsetTotemPole(int pinOffset) throws InvalidPinException {
 		return this.isPinTotemPole(pinOffset + 1);
 	}
 
 	public boolean isPinOffsetTriState(int pinOffset) throws InvalidPinException {
 		return this.isPinTriState(pinOffset + 1);
 	}
 
 	public boolean isPinOffsetDriven(int pinOffset) throws InvalidPinException {
 		return this.isPinDriven(pinOffset + 1);
 	}
 
 	public boolean isPowered() throws InvalidPinException {
 		int[] vccPins = this.getPinOffsets("VCC");
 		int[] gndPins = this.getPinOffsets("GND");
 		boolean result = true;
 
 		for (int i = 0; i < vccPins.length; i++) {
 			if (!this.chip.readTTL(vccPins[i]).equals("HIGH")) {
 				result = false;
 			}
 		}
 		for (int i = 0; i < gndPins.length; i++) {
 			if (!this.chip.readTTL(gndPins[i]).equals("LOW")) {
 				result = false;
 			}
 
 		}
 
 		return result;
 	}
 
 	public boolean isHigh(String input) throws InvalidPinException {
 		if (this.chip.readTTL(this.getPinOffset(input)).equals("HIGH")) {
 			this.getPin(input).setPinState(Pin.PinState.HIGH);
 
 			return true;
 		}
 
 		this.getPin(input).setPinState(Pin.PinState.LOW);
 
 		return false;
 	}
 
 	public boolean isLow(String input) throws InvalidPinException {
 		if (this.chip.readTTL(this.getPinOffset(input)).equals("HIGH")) {
 			this.getPin(input).setPinState(Pin.PinState.HIGH);
 
 			return false;
 		}
 
 		this.getPin(input).setPinState(Pin.PinState.LOW);
 
 		return true;
 	}
 
 	public boolean isStateHigh(String input) throws InvalidPinException {
 		if (this.getPin(input).getPinState().equals(Pin.PinState.HIGH)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	public boolean isStateLow(String input) throws InvalidPinException {
 		if (this.getPin(input).getPinState().equals(Pin.PinState.LOW)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	public Pin.PinState getPinState(String input) throws InvalidPinException {
 		return this.getPin(input).getPinState();
 	}
 
 	public boolean isRisingEdge(String input) throws InvalidPinException {
 		Pin.PinState currentState = this.getPinState(input);
 		if (this.isHigh(input) && currentState.equals(Pin.PinState.LOW)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	public boolean isFallingEdge(String input) throws InvalidPinException {
 		Pin.PinState currentState = this.getPinState(input);
 		if (this.isLow(input) && currentState.equals(Pin.PinState.HIGH)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	protected String updateConfigFilePath(String file) {
 		URL url = ClassLoader.getSystemResource("configFiles");
 		String urlName = url.getFile().substring(1);
 		int urlIndex = urlName.indexOf("configFiles");
 		String urlBase = urlName.substring(0, urlIndex);
 
 		int fileIndex = file.indexOf("configFiles");
 		String fileBase = file.substring(fileIndex);
 
 		String newFilename = urlBase + fileBase;
 
 		return newFilename;
 	}
 
 	@Override
 	public String getPinType(int pinOffset) {
 		try {
 			if (this.isPinOffsetInput(pinOffset)) {
				return "IN";
 			} else {
 				if (this.isPinOffsetClockOutput(pinOffset)) {
					return "CLO";
 				} else {
 					if (this.isPinOffsetInputOutput(pinOffset)) {
						return "IO";
 					} else {
 						if (this.isPinOffsetOpenCollector(pinOffset) || this.isPinOffsetTriState(pinOffset)) {
							return "OCO";
 						} else {
 							if (this.isPinOffsetOutput(pinOffset)) {
								return "OUT";
 							} else {
 								if (this.isPinOffsetPower(pinOffset)) {
									return "VCC";
 								}
 							}
 						}
 					}
 				}
 			}
 			return "NC";
 		} catch (InvalidPinException e1) {
 			System.out.println("OPPS: getPinType( int pinOffset " + pinOffset + " )");
 		}
 		return "NC";
 	}
 
 	@Override
 	public String[] getDerivatives() {
 		List<String> derivatives = new ArrayList<String>();
 
 		String className = this.getClass().toString();
 		className = className.substring(6);
 
 		String pathName = this.getClass().toString().replace('.', '/');
 		pathName = pathName.substring(6, pathName.lastIndexOf('/'));
 
 		URL url = ClassLoader.getSystemResource(pathName);
 
 		String fileName = url.getFile();
 		File directory = new File(fileName);
 
 		if (directory.exists()) {
 			String[] files = directory.list();
 
 			for (String file : files) {
 				if (file.endsWith(".class")) {
 					try {
 						Object thisObject = Class.forName(className);
 
 						String newName = (pathName + "/" + file.substring(0, file.length() - 6)).replace('/', '.');
 						try {
 							Object newObject = Class.forName(newName);
 
 							if (thisObject.getClass().isInstance(newObject)) {
 								derivatives.add(newObject.toString());
 							}
 						} catch (ClassNotFoundException e) {
 							System.out.println("New Class Not Found");
 						}
 					} catch (ClassNotFoundException e) {
 						System.out.println("This Class Not Found");
 					}
 				}
 			}
 		}
 		String[] returnData = derivatives.toArray(new String[derivatives.size()]);
 		return returnData;
 	}
 
 	public void setPin(String output, Pin.PinState state) throws InvalidPinException {
 		this.getPin(output).setPinState(state);
 		switch (state.ordinal()) {
 			case 1:
 				this.chip.write(this.getPinOffset(output), "HIGH", ((OutputPin) this.getPin(output)).getPinDelayTplh());
 
 				break;
 			case 2:
 				this.chip.write(this.getPinOffset(output), "LOW", ((OutputPin) this.getPin(output)).getPinDelayTphl());
 
 				break;
 			case 3:
 				this.chip.write(this.getPinOffset(output), "NC", 0);
 
 				break;
 			case 4:
 				this.chip.write(this.getPinOffset(output), "NC", 0);
 
 				break;
 			case 5:
 				break;
 		}
 	}
 
 	public void setPin(String output, Pin.PinState state, int delay) throws InvalidPinException {
 		this.getPin(output).setPinState(state);
 		int riseFallDelay = ((OutputPin) this.getPin(output)).getPinDelayTplh();
 		int timeDelay = riseFallDelay + delay;
 
 		switch (state.ordinal()) {
 			case 1:
 				this.chip.write(this.getPinOffset(output), "HIGH", timeDelay);
 
 				break;
 			case 2:
 				this.chip.write(this.getPinOffset(output), "LOW", timeDelay);
 
 				break;
 			case 3:
 				this.chip.write(this.getPinOffset(output), "NC", timeDelay);
 
 				break;
 			case 4:
 				this.chip.write(this.getPinOffset(output), "NC", timeDelay);
 
 				break;
 			case 5:
 				break;
 		}
 	}
 
 	@Override
 	public int getDerivative() {
 		return 0;
 	}
 
 	@Override
 	public void setDerivative(int t) {}
 
 	@Override
 	public String[] getPackages() {
 		String[] dummy = { "", "" };
 		return dummy;
 	}
 
 	@Override
 	public int getPackage() {
 		return 0;
 	}
 
 	@Override
 	public void setPackage(int p) {}
 }
