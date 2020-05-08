 import java.util.*;
 import java.nio.file.*;
 import java.nio.charset.*;
 
 public class ChallengeSheet {
 
 	public static void makeSheet() {
 
 		Difficulty[] ds = {
 			Difficulty.getEasyDifficulty(), 
 			Difficulty.getMediumDifficulty(), 
 			Difficulty.getHardDifficulty()
 		};
 
 		Challenge[] cs = new Challenge[3];
 
 		{
 			int found = 0;
 			while (found != 3) {
 				Challenge c = new Challenge(ds[found]);
 				ArrayList<Operator> operators = c.getChallenge();
 				boolean fail = false;
 
 				// Throw away bad Challenges that use the same operator twice in a row
 				for (int i = 2; i < operators.size(); i++) {
 					if (operators.get(i).getClass().equals(operators.get(i-1).getClass())) {
 						fail = true;
 					}
 				}
 
 				if (!fail) {
 					cs[found++] = c;
 				}
 			}
 		}
 
 		int[] solutions = { cs[0].getSolution(), cs[1].getSolution(), cs[2].getSolution() };
 
 		// Open Files
 
 		List<String> head = null;
 		List<String> tail = null;
 		List<String> chHead = null;
 		List<String> chTail = null;
 
 		try {
 			Path headP = FileSystems.getDefault().getPath("tex", "head.tex");
 			Path tailP = FileSystems.getDefault().getPath("tex", "tail.tex");
 			Path chHeadP = FileSystems.getDefault().getPath("tex", "challengeBefore.tex");
 			Path chTailP = FileSystems.getDefault().getPath("tex", "challengeAfter.tex");
 
 			Charset charset = Charset.defaultCharset();
 			head = Files.readAllLines(headP, charset);
 			tail = Files.readAllLines(tailP, charset);
 			chHead = Files.readAllLines(chHeadP, charset);
 			chTail = Files.readAllLines(chTailP, charset);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// Print Document
 
 		for (String s : head) {
 			System.out.println(s);
 		}
 
 		for (Challenge c : cs) {
 			
 			for (String s : chHead) {
 				System.out.println(s);
 			}
 
 			for (Operator o : c.getChallenge()) {
 				System.out.print(o.toTexString());
 				System.out.print(" & ");
 			}
 			System.out.println("\\textcolor{white}{?} \\\\");
 
 			for (String s : chTail) {
 				System.out.println(s);
 			}
 		}
 
 		System.out.println("\\begin{turn}{180}");
 		System.out.format("Lösungen: %d, %d, %d\n", solutions[0], solutions[1], solutions[2]);
		System.out.println("\\end{turn}{180}");
 
 
 		for (String s : tail) {
 			System.out.println(s);
 		}
 	}
 }
