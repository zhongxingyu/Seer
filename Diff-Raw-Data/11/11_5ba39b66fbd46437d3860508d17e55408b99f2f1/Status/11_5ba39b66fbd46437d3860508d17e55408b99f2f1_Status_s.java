 package uk.co.dbyz.mc.CreeperHostAPI;
 
 /**
  * An object representing server status.
  */
 public class Status {
 	private static String METHOD = "Status";
 	private int RAM;
 	private int CPU;
 	private int HDD;
 	private boolean UP;
 
 	/**
 	 * Gets the status of the server
 	 * @return A {@link Status Status} representing the servers status
 	 */
 	public static Status getStatus() {
 		String str = Common.doGet(METHOD,"Get").trim();
 		String[] status = str.split(",");
 		try {
 			int ram = Integer.parseInt(status[0]);
 			int cpu = Integer.parseInt(status[1]);
 			int hdd = Integer.parseInt(status[2]);
 			boolean up = (status[3].equalsIgnoreCase("1") ? true : false);
 
 			return new Status(ram, cpu, hdd, up);
 		} catch (NumberFormatException nfe) {
 			return null;
 		}
 	}
 
 	private Status(int ram, int cpu, int hdd, boolean up) {
 		this.RAM = ram;
 		this.CPU = cpu;
 		this.HDD = hdd;
 		this.UP = up;
 	}
 
 	/**
 	 * The current percentage of RAM in use.
	 * @param The current percentage of RAM in use.
 	 */
 	public int getRAM() {
 		return RAM;
 	}
 
 	/**
 	 * The current percentage of CPU in use.
	 * @param The current percentage of CPU in use.
 	 */
 	public int getCPU() {
 		return CPU;
 	}
 
 	/**
 	 * The current percentage of HDD Space in use.
	 * @param The current percentage of HDD Space in use.
 	 */
 	public int getHDD() {
 		return HDD;
 	}
 
 	/**
 	 * Is the server UP.
	 * @param Is the server UP.
 	 */
 	public boolean isUP() {
 		return UP;
 	}
 }
