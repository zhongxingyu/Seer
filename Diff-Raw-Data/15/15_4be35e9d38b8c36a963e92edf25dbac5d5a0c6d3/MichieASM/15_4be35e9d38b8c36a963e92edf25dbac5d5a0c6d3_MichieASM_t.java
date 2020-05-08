 package pl.ijestfajnie.circles.kernel;
 
 import pl.ijestfajnie.circles.CircleCommands;
 import pl.ijestfajnie.circles.DebugManager;
 
 public class MichieASM {
 	
 	public static int parse(int process_id, String[] cartridge, int currentPointer) {
 		switch (cartridge[currentPointer]) {
 			case CircleCommands.CMD_ADD:
 				int toadd = Integer.parseInt(cartridge[++currentPointer], 2);
 				int pointer1 = Integer.parseInt(cartridge[++currentPointer], 2);
 				String[] args = {String.valueOf(pointer1)};
 				int value = Kernel.SystemCallValue("getram", args);
 				value += toadd;
 				String[] args2 = {String.valueOf(pointer1), String.valueOf(value)};
 				Kernel.SystemCall("setram", args2);
 				break;
 			case CircleCommands.CMD_SUB:
 				int tosub = Integer.parseInt(cartridge[++currentPointer], 2);
 				int pointer2 = Integer.parseInt(cartridge[++currentPointer], 2);
 				String[] args3 = {String.valueOf(pointer2)};
 				int value2 = Kernel.SystemCallValue("getram", args3);
 				value2 -= tosub;
 				String[] args4 = {String.valueOf(pointer2), String.valueOf(value2)};
 				Kernel.SystemCall("setram", args4);
 				break;
 			case CircleCommands.CMD_SET:
 				int toset = Integer.parseInt(cartridge[++currentPointer], 2);
 				int pointer3 = Integer.parseInt(cartridge[++currentPointer], 2);
 				String[] args5 = {String.valueOf(pointer3), String.valueOf(toset)};
 				Kernel.SystemCall("setram", args5);
 				break;
 			case CircleCommands.CMD_SAD:
 				int tosad = Integer.parseInt(cartridge[++currentPointer], 2);
 				int pointer4 = Integer.parseInt(cartridge[++currentPointer], 2);
 				String[] args6 = {String.valueOf(pointer4)};
 				int value3 = Kernel.SystemCallValue("getram", args6);
 				value3 += tosad;
 				String[] args7 = {String.valueOf(pointer4), String.valueOf(value3)};
 				Kernel.SystemCall("setvideoram", args7);
 				break;
 			case CircleCommands.CMD_SSU:
 				int tossu = Integer.parseInt(cartridge[++currentPointer], 2);
 				int pointer5 = Integer.parseInt(cartridge[++currentPointer], 2);
 				String[] args8 = {String.valueOf(pointer5)};
 				int value4 = Kernel.SystemCallValue("getram", args8);
 				value4 += tossu;
 				String[] args9 = {String.valueOf(pointer5), String.valueOf(value4)};
 				Kernel.SystemCall("setvideoram", args9);
 				break;
 			case CircleCommands.CMD_SST:
 				int tosst = Integer.parseInt(cartridge[++currentPointer], 2);
 				int pointer6 = Integer.parseInt(cartridge[++currentPointer], 2);
 				String[] args10 = {String.valueOf(pointer6), String.valueOf(tosst)};
 				/*System.out.println(*/Kernel.SystemCall("setvideoram", args10)/*)*/;
 				break;
 			case CircleCommands.CMD_FWD:
 				int tofwd = Integer.parseInt(cartridge[++currentPointer], 2);
 				currentPointer += tofwd;
 				break;
 			case CircleCommands.CMD_BCK:
 				int tobck = Integer.parseInt(cartridge[++currentPointer], 2);
 				currentPointer -= tobck;
 				break;
 			case CircleCommands.CMD_CHK:
 				int chktype = Integer.parseInt(cartridge[++currentPointer], 2);
 				int onecell = Integer.parseInt(cartridge[++currentPointer], 2);
 				String[] args11 = {String.valueOf(onecell)};
 				int onevalu = Kernel.SystemCallValue("getram", args11);
 				int twocell = Integer.parseInt(cartridge[++currentPointer], 2);
 				String[] args12 = {String.valueOf(twocell)};
 				int twovalu = Kernel.SystemCallValue("getram", args12);
 				if (chktype == Integer.parseInt(CircleCommands.ConditionType.CND_EQL, 2)) {
 					if (onevalu == twovalu) {
 						currentPointer++;
 					} else {
 						int toskip = Integer.parseInt(cartridge[++currentPointer], 2);
 						currentPointer += toskip;
 					}
 				} else if (chktype == Integer.parseInt(CircleCommands.ConditionType.CND_MOR, 2)) {
 					if (onevalu > twovalu) {
 						currentPointer++;
 					} else {
 						int toskip = Integer.parseInt(cartridge[++currentPointer], 2);
 						currentPointer += toskip;
 					}
 				} else if (chktype == Integer.parseInt(CircleCommands.ConditionType.CND_LES, 2)) {
 					if (onevalu < twovalu) {
 						currentPointer++;
 					} else {
 						int toskip = Integer.parseInt(cartridge[++currentPointer], 2);
 						currentPointer += toskip;
 					}
 				} else if (chktype == Integer.parseInt(CircleCommands.ConditionType.CND_MOE, 2)) {
 					if (onevalu >= twovalu) {
 						currentPointer++;
 					} else {
 						int toskip = Integer.parseInt(cartridge[++currentPointer], 2);
 						currentPointer += toskip;
 					}
 				} else if (chktype == Integer.parseInt(CircleCommands.ConditionType.CND_LOE, 2)) {
 					if (onevalu <= twovalu) {
 						currentPointer++;
 					} else {
 						int toskip = Integer.parseInt(cartridge[++currentPointer], 2);
 						currentPointer += toskip;
 					}
 				} else if (chktype == Integer.parseInt(CircleCommands.ConditionType.CND_NOT, 2)) {
 					if (onevalu != twovalu) {
 						currentPointer++;
 					} else {
 						int toskip = Integer.parseInt(cartridge[++currentPointer], 2);
 						currentPointer += toskip;
 					}
 				}
 				break;
 			default:
 				break;
 		}
 		return currentPointer;
 	}
 }
