 class BoardImpl implements Board {
 	
 	private Rule rule;
 
 	private boolean[][] states;
 
 	public BoardImpl(Rule rule) {
 		this.rule=rule;
 	}
 
 	public void set(boolean[][] states) {
 		this.states = states;
 		
 	}
 	
 	public boolean[][] tick() {
 		boolean [][] next = createEmptyState();
 		for(int line =0; line < states.length; line++ ) {
 			for(int row=0; row< states[line].length; row++ ) {
 				int neighbours= countNeighbours(line,row);
 				next[line][row]=rule.calculate(states[line][row],neighbours);
 			}			
 		}
		return next;
 	}
 
 	private boolean[][] createEmptyState() {
 		return new boolean[states.length][states[0].length];
 	}
 
 	private int countNeighbours(int line, int row) {
 		int count = 0;
 		
 		for(int lineOffset=-1; lineOffset <=1; lineOffset++) {
 			int lineIndex = line+lineOffset;
 			if(lineIndex <0 || lineIndex >= states.length) continue;
 			for(int rowOffset=-1; rowOffset <=1; rowOffset++) {
 				
 				if(lineOffset==0 && rowOffset==0) continue;
 				int rowIndex = row+rowOffset;
 				if(rowIndex <0 || rowIndex >= states[lineIndex].length) continue;
 				if( states[lineIndex][rowIndex]) {
 					count++;
 				}
 			}
 		}
 		return count;
 	}
 }
