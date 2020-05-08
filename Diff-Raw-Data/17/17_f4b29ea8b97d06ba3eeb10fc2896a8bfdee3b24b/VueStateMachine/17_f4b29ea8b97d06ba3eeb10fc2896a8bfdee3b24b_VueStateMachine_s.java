 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller.terminal.controller;
 
 import controller.terminal.interfacesGUI.TerminalSubVue;
 import model.database.BikeUsageMapper;
 import model.database.ReturnAmountMapper;
 
 /**
  *
  * @author Valentin SEITZ
  */
 class VueStateMachine {
 
 	//The vues, first dimensions of possibilites array
 	public static final int IMPOSSIBLE = -2;
 	public static final int VUE_WELCOME = 0;
 	public static final int VUE_END = VUE_WELCOME;
 	public static final int VUE_RENT = 1;
 	public static final int VUE_RENT_SUMMARY = 2;
 	public static final int VUE_RENT_PAY = 3;
 	public static final int VUE_RETURN = 4;
 	public static final int VUE_RETURN_SUMMARY = 5;
 	public static final int VUE_RETURN_PAY = 6;
 	//The actions which can change the vue, second dimension of possibilites
 	public static final int ACTION_DO_CANCEL = 0;
 	public static final int ACTION_ASK_RENT = 1;
 	public static final int ACTION_DO_RENT = 2;
 	public static final int ACTION_ASK_RETURN = 3;
 	public static final int ACTION_DO_RETURN = 4;
 	public static final int ACTION_ASK_PAY = 5;
 	public static final int ACTION_DO_PAY = 6;
 	public static final int ACTION_DO_FINISH = 7;
 	//The possibilites array
 	private static int[][] possibilities = {
 		{VUE_WELCOME, VUE_RENT, IMPOSSIBLE, VUE_RETURN, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE}, //Welcome & End
 		{VUE_WELCOME, IMPOSSIBLE, VUE_RENT_SUMMARY, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE}, //Rent
 		{VUE_WELCOME, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, VUE_RENT_PAY, IMPOSSIBLE, IMPOSSIBLE}, //Rent Summary
 		{VUE_WELCOME, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, VUE_END, IMPOSSIBLE}, //Rent Pay
 		{VUE_WELCOME, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, VUE_RETURN_SUMMARY, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE}, //Return
 		{VUE_WELCOME, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, VUE_RETURN_PAY, IMPOSSIBLE, VUE_END}, //Return summary
 		{VUE_WELCOME, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, VUE_END, IMPOSSIBLE} //Return Pay
 	};
 	//The state of machine
 	private static int state = VUE_WELCOME;
 
 	public static int getState() {
 		return state;
 	}
 
 	public static boolean possibleAction(int action) {
 		return change(action, false);
 	}
 
 	public static boolean doAction(int action) {
 		return change(action, true);
 	}
 
 	private static boolean change(int action, boolean validate) {
 		boolean possible;
 		int nextState;
 		//State index ok?
 		possible = (0 <= state && state < possibilities.length);
 		if (possible) {
 			//Action index ok?
 			possible = (0 <= action && action < possibilities[state].length);
 		}
 		if (possible) {
 			//Reading next state, if not impossible
 			nextState = possibilities[state][action];
 			possible = (nextState != IMPOSSIBLE);
 			if (possible) {
 				if (validate) {
				state = nextState;
 					displayVue(state);
 				}
 			} else {
 				TerminalController.getMainVue().showError("Impossible d'effectuer cette action!");
 			}
 		}
 		return possible;
 	}
 
 	private static void displayVue(int state) {
 		TerminalSubVue currentVue;
 		switch (state) {
 			case VUE_WELCOME:
 				//case VUE_END: (Same as welcome)
				VueStateMachine.doCancel(); //Cancel all processed data
 				TerminalController.getMainVue().displayTerminalWelcome();
 				currentVue = TerminalController.getMainVue().getTerminalWelcome();
 				break;
 			case VUE_RENT:
 				TerminalController.getMainVue().displayTerminalRent();
 				currentVue = TerminalController.getMainVue().getTerminalRent();
 				break;
 			case VUE_RENT_SUMMARY:
 				TerminalController.getMainVue().displayTerminalRentSummary();
 				currentVue = TerminalController.getMainVue().getTerminalRentSummary();
 				break;
 			case VUE_RETURN:
 				TerminalController.getMainVue().displayTerminalReturn();
 				currentVue = TerminalController.getMainVue().getTerminalReturn();
 				break;
 			case VUE_RETURN_SUMMARY:
 				TerminalController.getMainVue().displayTerminalReturnSummary();
 				currentVue = TerminalController.getMainVue().getTerminalReturnSummary();
 				break;
 			case VUE_RENT_PAY:
 			case VUE_RETURN_PAY:
 				TerminalController.getMainVue().displayTerminalPay();
 				currentVue = TerminalController.getMainVue().getTerminalPay();
 				break;
 			default:
 				TerminalController.getMainVue().displayTerminalWelcome();
 				currentVue = TerminalController.getMainVue().getTerminalWelcome();
 		}
 		if (currentVue != null) {
 			currentVue.init();
 		}
 	}
 
	public static void doCancel() {
 		switch (VueStateMachine.getState()) {
 			case VUE_RENT_SUMMARY:
 			case VUE_RENT_PAY:
 				BikeUsageMapper bum = new BikeUsageMapper();
 				bum.resetBikesLocationProcess(ProcessedData.getIdBikeUsagesToResetEndDate(), ProcessedData.getIdBikeUsagesToDelete());
 				break;
 			case VUE_RETURN_SUMMARY:
 			case VUE_RETURN_PAY:
 				ReturnAmountMapper ram = new ReturnAmountMapper();
 				ram.deleteReturnAmountById(ProcessedData.getIdReturnAmountToDelete());
 				break;
 			case VUE_WELCOME:
			case VUE_RENT:
			case VUE_RETURN:
 				//Nothing to do
 				break;
 			default:
 		}
 		ProcessedData.doResetData();
 	}
 }
