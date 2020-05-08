 package fi.helsinki.cs.koskelo.analyser;
 
 import fi.hu.cs.ttk91.TTK91CompileSource;
 import fi.hu.cs.ttk91.TTK91Cpu;
 import fi.hu.cs.ttk91.TTK91Memory;
 import fi.hu.cs.ttk91.TTK91Exception;
 import fi.hu.cs.ttk91.TTK91Application;
 import fi.hu.cs.ttk91.TTK91Core;
 
 
 import fi.hu.cs.titokone.Source;
 import fi.hu.cs.titokone.Control;
 import fi.hu.cs.titokone.Processor;
 import fi.hu.cs.titokone.RandomAccessMemory;
 import fi.hu.cs.titokone.MemoryLine;
 import fi.helsinki.cs.koskelo.common.TTK91TaskOptions;
 import fi.helsinki.cs.koskelo.common.TTK91Constant;
 
 public class TTK91AnalyseData{
 
 
 	// Controllit joilta saadaan dataa:
 
 	private TTK91Core controlCompiler = null;    
 	private TTK91Core controlPublicInputStudent = null;
 	// publicinputeilla tai ilman inputteja opiskelijan vastaus
 	private TTK91Core controlPublicInputTeacher = null; 
 	// publicinputeilla tai ilman inputteja malliratkaisu jos vertailu on mritelty simuloitavaksi
 	private TTK91Core controlHiddenInputStudent = null;
 	// hiddeninputeilla jos ovat mritelty opiskelijan vastaus
 	private TTK91Core controlHiddenInputTeacher = null;
 	// hiddeninputeilla jos ovat mritelty malliratkaisu jos vertailu on mritelty simuloitavaksi
 
 	// Tehtvnmrittelydata
 	private TTK91TaskOptions taskOptions = null;        // taskoptions
 
 	// Malliratkaisu
 
 	private String exampleCode = null;                // haetaan taskoptionsista - malliratkaisun koodi
 	private String answer = null;
 
 	// Virheilmoitukset
 
 	private boolean errors = false;
 	private String studentCompileError = null;
 	private String teacherCompileError = null;
 	private String studentRunError = null;
 	private String teacherRunError = null;
 
 	// Knnetyt koodit
 	private TTK91Application studentApplicationPublic = null; // opiskelijan vastaus
 	private TTK91Application teacherApplicationPublic = null; // malliratkaisu
 	private TTK91Application studentApplicationHidden = null; // opiskelijan vastaus
 	private TTK91Application teacherApplicationHidden = null; // malliratkaisu
 
 
 	// Analysointitapa
 	private int compareMethod = -1;
 
 
 	// Optionsin muuttujia:
 
 	private int steps = 0;
 	private String publicInput = null;
 	private String hiddenInput = null;
 
 
 	// Opiskelijan tietokoneen statistiikkamuuttujat
 
 	private int dataSegmentSize;
 	private int codeSegmentSize;
 
 	//Analysointia varten
 
 	private int maxStackSize;
 	private int usedCommands;
 	private int memoryReferences;
 	private int hiddenUsedCommands;
 	private int hiddenMemoryReferences;
 
 
 	// Titokoneiden muistit (FIXME, nit ei taideta tarvita?):
 
 	private    RandomAccessMemory studentPublicMemory = null; 
 	private    RandomAccessMemory teacherPublicMemory = null; 
 	private    RandomAccessMemory studentHiddenMemory = null; 
 	private    RandomAccessMemory teacherHiddenMemory = null;
 
 
 	public TTK91AnalyseData(
 			TTK91TaskOptions taskOptions,
 			String answer, 
 			String exampleCode
 			) {
 
 		this.taskOptions = taskOptions;
 		this.answer = answer;
 		this.exampleCode = exampleCode;
 
 		compileTeacherApplication();
 		compileStudentApplication();
 		getTaskData();
 
 		// compilet ovat voineet ptty virheeseen
 		if(!this.errors) {
 			run();
 		}
 
 		// ohjelmien ajo on voinut ptty virheeseen
 		if(!this.errors) {
 			setStatistics();
 		}
 	}
 
 
 	private void setStatistics() {
 
 		if(controlPublicInputStudent != null && !errors) {
 
 			// FIXME, tm on titokoneen rajapinnan puutteen kiertoa!!
 			RandomAccessMemory ram = (RandomAccessMemory) 
 				controlPublicInputStudent.getMemory();
 
 			Processor cpu = (Processor)
 				controlPublicInputStudent.getCpu(); 
 
 			usedCommands = cpu.giveCommAmount();
 			maxStackSize = cpu.giveStackMaxSize();
 			memoryReferences = ram.getMemoryReferences();
 			codeSegmentSize = ram.getCodeAreaSize();
 			dataSegmentSize = ram.getDataAreaSize();
 
 			if(controlHiddenInputStudent != null) {
 
 				// FIXME, tm on titokoneen rajapinnan puutteen kiertoa!!
 				ram = (RandomAccessMemory) 
 					controlHiddenInputStudent.getMemory();
 
 				cpu = (Processor)
 					controlHiddenInputStudent.getCpu(); 
 
 				hiddenUsedCommands = cpu.giveCommAmount();
 				hiddenMemoryReferences = ram.getMemoryReferences();
 
 			} 
 
 		}
 	}
 
 	private void compileTeacherApplication() {
 
 
 		// compile teacherapp
 
 		TTK91CompileSource src = null;
 
 		if(exampleCode == null) {
 			return; // FIXME: virheenksittely
 		}
 
 
 		src = (TTK91CompileSource) new Source(exampleCode);
 
 		if (src == null) {
 			this.teacherCompileError = "Malliratkaisua ei pystytty"+
 				" muuntamaan TTK91CompileSource-muotoon";
 		}//if
 
 
 		TTK91Application app = null;
 
 		if (controlCompiler == null) {
 			controlCompiler = new Control(null, null);
 		}
 
 		try {
 			app = controlCompiler.compile(src);
 		} catch (TTK91Exception e) {
			this.teacherCompileError = "Teacher compile error:" + 
 				e.getMessage();
 			this.errors = true;
 			return;
 		}//catch
 
 		this.teacherApplicationPublic = app;
 
 		try {
 			app = controlCompiler.compile(src);
 		} catch (TTK91Exception e) {
			this.teacherCompileError = "Teacher compile error:" + 
 				e.getMessage();
 			this.errors = true;
 			return;
 		}//catch
 
 		this.teacherApplicationHidden = app;
 
 		// get the student app and the
 		// teacher app and make them into applications
 
 	}
 
 	private void compileStudentApplication() {
 
 
 		// compile studentapp
 
 		TTK91CompileSource src = null;
 
 		if (this.answer != null) {
 			String ans = this.answer;
 			ans = ans + "\n SVC SP, =HALT";
 			src = (TTK91CompileSource) new Source(ans);
 			// FIXME: toimiiko tosiaan nin helposti?
 		}
 
 		if (src == null) {
 			this.studentCompileError = "Ratkaisua ei pystytty"+
 				" muuntamaan TTK91CompileSource-muotoon";
 			this.errors = true;
 		}//if
 
 
 		TTK91Application app = null;
 
 		if (controlCompiler == null) {
 			controlCompiler = new Control(null, null);
 		}
 
 		try {
 			app = controlCompiler.compile(src);
 		} catch (TTK91Exception e) {
			this.studentCompileError = "Student compile error:" + 
 				e.getMessage();
 			this.errors = true;
 			return;
 		}//catch
 
 		this.studentApplicationPublic = app;
 
 		try {
 			app = controlCompiler.compile(src);
 		} catch (TTK91Exception e) {
			this.studentCompileError = "Student compile error:" + 
 				e.getMessage();
 			this.errors = true;
 			return;
 		}//catch
 
 		this.studentApplicationHidden = app;
 
 		// get the student app and the
 		// teacher app and make them into applications
 
 	}
 
 	private void getTaskData() {
 		this.publicInput = parseInputString(
 				this.taskOptions.getPublicInput()
 				);
 		this.hiddenInput = parseInputString(
 				this.taskOptions.getHiddenInput()
 				);
 
 		this.steps = taskOptions.getMaxCommands();
 
 		this.compareMethod = taskOptions.getCompareMethod();
 	
 		System.err.println("Vertailumetodi luettiin: " + compareMethod);
 	}
 
 
 
 	private void run() {
 
 
 		/* Koska titokoneesta metodilla .getCPU() saadaan
 		 * vain viite controlin sisiseen prosessiin kytetn
 		 * yhteens maksissaan nelj controlia, kuitenkin
 		 * siten, ett kullekin simulointikierrokselle
 		 * luodaan oma controlinsa.
 		 */
 
 		if(publicInput != null) {
 
 			this.studentApplicationPublic.setKbd(publicInput);
 			if(compareMethod == taskOptions.COMPARE_TO_SIMULATED) {
 				this.teacherApplicationPublic.setKbd(
 						publicInput
 						);
 			}
 		}
 
 		this.controlPublicInputStudent = new Control(null, null);
 
 		try {
 			this.controlPublicInputStudent.run(
 					this.studentApplicationPublic, 
 					steps
 					);
 			// 1. simulointi
 		} catch (TTK91Exception e) {
		    this.studentRunError = "Student run error:"+ 
			e.getMessage();
		    this.errors = true;
		    return;
 		}
 
 		if(compareMethod == TTK91Constant.COMPARE_TO_SIMULATED) {
 			// 1. simulointi malliratkaisua
 			System.err.println("Simuloituvertailu");
 			
 			this.controlPublicInputTeacher = new Control(null, null);
 			
 			try {
 				this.controlPublicInputTeacher.run(
 						this.teacherApplicationPublic,
 						steps
 						);
 			} catch (TTK91Exception e) {
				this.teacherRunError = "Teacher run error:" +
 					e.getMessage();
 				this.errors = true;
 				return;
 			}
 		}
 
 
 		if (hiddenInput != null) {
 			// mahdollinen 2. simulointi opiskelijan ratkaisusta
 
 			this.controlHiddenInputStudent = new Control(null, null); // luodaan control vain jos hiddeninput mritelty --> "optimointia"
 			this.studentApplicationHidden.setKbd(hiddenInput);
 
 			try {
 				this.controlHiddenInputStudent.run(
 						this.studentApplicationHidden,
 						steps
 						);
 
 			} catch (TTK91Exception e) {
				this.studentRunError = "Student run error:" + 
 					e.getMessage();
 				this.errors = true;
 				return;
 			}
 
 			if(compareMethod == TTK91Constant.COMPARE_TO_SIMULATED) {
 				// simuloidaa malliratkaisu
 				// 2. simulointi malliratkaisua
 
 				this.controlHiddenInputTeacher = new Control(null, null); // luodaan control vain jos hiddeninput mritelty --> "optimointia"
 				this.teacherApplicationHidden.setKbd(
 						hiddenInput
 						);
 				try {
 					this.controlHiddenInputTeacher.run(
 							this.teacherApplicationHidden,
 							steps
 							);
 				} catch (TTK91Exception e) {
					teacherRunError = "Teacher run error:" +
 						e.getMessage();
 					this.errors = true;
 					return;
 				}
 			}
 		} 
 
 
 
 		// Aja ohjelmat.
 		// Aseta muisti,
 		// Aseta rekisterit,
 		// Aseta tulosteet.
 
 	}
 
 	private String parseInputString(int[] inputTable) {
 
 	    if (inputTable == null) {
 		return null;
 	    }
 		String input = "";
 		
 		for(int i = 0; i < inputTable.length; i++) {
 			input = input + inputTable[i];
 		}
 
 		if(input.equals("")){
 			return null;
 		} else {
 			return input;
 		}
 	}
 
 	/* Tarvitaan viel: sopivat getterit */
 
 	public String getStudentCompileError() {
 		return this.studentCompileError;
 	}
 
 	public String getTeacherCompileError() {
 		return this.teacherCompileError;
 	}
 
 	public String getStudentRunError() {
 		return this.studentRunError;
 	}
 
 	public String getTeacherRunError() {
 		return this.teacherRunError;
 	}
 
 	public String[] getErrorMessages() { //FIXME rumaa!
 		String[] errors = new String[4];
 		errors[0] = studentCompileError;
 		errors[1] = teacherCompileError;
 		errors[2] = studentRunError;
 		errors[3] = teacherRunError;
 
 		return errors;
 	}
 
 	public boolean errors() {
 		return this.errors;
 	}
 
 	public boolean compareToHidden() {
 		return (hiddenInput != null);
 	}
 
 	public String getAnswer() {
 		return this.answer;
 	}
 
 	public TTK91Application getStudentAppPublic() {
 		return this.studentApplicationPublic;
 	}
 
 	public TTK91Application getTeacherAppPublic() {
 		return this.teacherApplicationPublic;
 	}
 
 	public TTK91Application getStudentAppHidden() {
 		return this.studentApplicationHidden;
 	}
 
 	public TTK91Application getTeacherAppHidden() {
 		return this.teacherApplicationHidden;
 	}
 
 	public TTK91Cpu getStudentCpuPublic(){
 		return controlPublicInputStudent.getCpu();
 	}
 	public TTK91Cpu getStudentCpuHidden(){
 
 		if(controlHiddenInputStudent != null) {
 			return controlHiddenInputStudent.getCpu();
 		} else {
 			return null;
 		}
 	}
 
 	public TTK91Cpu getTeacherCpuPublic(){
 		if(controlPublicInputTeacher != null) {
 		return controlPublicInputTeacher.getCpu();
 		} else {
 			return null;
 		}
 	}
 
 	public TTK91Cpu getTeacherCpuHidden(){
 
 		if(controlHiddenInputTeacher != null) {
 			return controlHiddenInputTeacher.getCpu();
 		} else {
 			return null;
 		}
 	}
 
 	public TTK91Memory getStudentMemoryPublic(){
 		return controlPublicInputStudent.getMemory();
 	}
 	public TTK91Memory getStudentMemoryHidden(){
 
 		if(controlHiddenInputStudent != null) {
 			return controlHiddenInputStudent.getMemory();
 		} else {
 			return null;
 		}
 	}
 
 	public TTK91Memory getTeacherMemoryPublic(){
 		if(controlPublicInputTeacher != null) {
 			return controlPublicInputTeacher.getMemory();
 		} else {
 			return null;
 		}
 	}
 
 	public TTK91Memory getTeacherMemoryHidden(){
 
 		if(controlHiddenInputTeacher != null) {
 			return controlHiddenInputTeacher.getMemory();
 		} else {
 			return null;
 		}
 	}
 
 
 	public TTK91TaskOptions getTaskOptions() {
 		return taskOptions;
 	}
 
 	public int getCommandAmount() {
 
 		return  this.usedCommands;
 
 	}
 
 	public int getHiddenCommandAmount() {
 
 		return  this.hiddenUsedCommands;
 
 	}
 
 	public int getStackSize() {
 
 		return this.maxStackSize;
 
 	}
 
 	public int getMemoryReferences() {
 		return this.memoryReferences;
 	}
 
 
 	public int getHiddenMemoryReferences() {
 		return this.hiddenMemoryReferences;
 	}
 
 
 	public int getCodeSegmentSize() {
 		return this.codeSegmentSize;
 	}
 
 	public int getDataSegmentSize() {
 		return this.dataSegmentSize;
 	}
 
 	public int analyseSwitcher(){
 
 		boolean compareToHidden = compareToHidden();
 		boolean compareToStatic = 
 			(compareMethod == TTK91Constant.COMPARE_TO_STATIC);
 		int compareCase = -1;
 
 		if ( compareToStatic && compareToHidden ) {
 			compareCase = TTK91Constant.COMPARE_TO_STATIC_HIDDEN;
 		}
 		else if ( compareToStatic && (!compareToHidden) ) {
 			compareCase = TTK91Constant.COMPARE_TO_STATIC_PUBLIC;
 		}
 		else if ( (!compareToStatic) && compareToHidden ) {
 			compareCase = TTK91Constant.COMPARE_TO_SIMULATED_HIDDEN;
 		}
 		else if ( (!compareToStatic) && (!compareToHidden) ) {
 			compareCase = TTK91Constant.COMPARE_TO_SIMULATED_PUBLIC;
 		}
 	
 		return compareCase;
 	
 	}
 
     public String getStudentScreenOutputPublic(){
 	if (studentApplicationPublic != null) {
 	    return studentApplicationPublic.readCrt();
 	} else {
 	    return null;
 	}
     }
 
     public String getStudentScreenOutputHidden(){
 	if (studentApplicationHidden != null && controlHiddenInputStudent != null) {
 	    return studentApplicationHidden.readCrt();
 	} else {
 	    return null;
 	}
     }
     
     public String getTeacherScreenOutputPublic(){
 	if (teacherApplicationPublic != null && controlPublicInputStudent != null) {
 	    return teacherApplicationPublic.readCrt();
 	} else {
 	    return null;
 	}
     }
     
     public String getTeacherScreenOutputHidden(){
 	if (teacherApplicationHidden != null && controlHiddenInputTeacher != null) {
 	    return teacherApplicationHidden.readCrt();
 	} else {
 	    return null;
 	}
     }
 
     public String getStudentFileOutputPublic(){
 	if (studentApplicationPublic != null) {
 	    return studentApplicationPublic.readStdOut();
 	} else {
 	    return null;
 	}
     }
 
     public String getStudentFileOutputHidden(){
 	if (studentApplicationHidden != null) {
 	    return studentApplicationHidden.readStdOut();
 	} else {
 	    return null;
 	}
     }
     
     public String getTeacherFileOutputPublic(){
 	if (teacherApplicationPublic != null) {
 	    return teacherApplicationPublic.readStdOut();
 	} else {
 	    return null;
 	}
     }
     
     public String getTeacherFileOutputHidden(){
 	if (teacherApplicationHidden != null) {
 	    return teacherApplicationHidden.readStdOut();
 	} else {
 	    return null;
 	}
     }
     
 }// class
