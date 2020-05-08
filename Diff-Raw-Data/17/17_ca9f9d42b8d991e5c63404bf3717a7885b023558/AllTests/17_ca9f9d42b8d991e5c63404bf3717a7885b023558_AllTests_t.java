 public class AllTests {
 
 	public static void main(String[] args) {
 		AdvancedPointerAnalysis.main(args);
 		ArithmeticTests.main(args);
 		ConditionalTests.main(args);
 		ConstantTests.main(args);
 		// IntervalTests is a proper unit test class, it does not require soot
 		LoopTests.main(args);
 		OtherTeamTests.main(args);
 		PointerAnalysisTests.main(args);
 		SoftDeadlineTests.main(args);
 		StaticFieldTests.main(args);
 		TemplateTests.main(args);
 		WideningTests.main(args);
		ImprecisionTests.main(args);
 	}
 
 }
