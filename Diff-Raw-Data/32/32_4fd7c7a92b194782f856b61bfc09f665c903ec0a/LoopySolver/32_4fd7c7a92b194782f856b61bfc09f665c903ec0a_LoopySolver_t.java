package                                                                                                                                                                                                                                                                                      edu.berkeley.gamesman.solver;
 
 import java.util.List;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.Value;
 import edu.berkeley.gamesman.core.WorkUnit;
 import edu.berkeley.gamesman.database.DatabaseHandle;
 import edu.berkeley.gamesman.game.LoopyMutaGame;
 import edu.berkeley.gamesman.game.LoopyRecord;
 import edu.berkeley.gamesman.util.qll.Pool;
 import edu.berkeley.gamesman.util.qll.Factory;
 
 /**
  * @author David, Brent, Nancy, Kevin, Peter, Sharmishtha, Raji
  *
  */
 public class LoopySolver extends Solver {
 	Pool<LoopyRecord> recordPool;
 
 	public LoopySolver(Configuration conf) {
 		super(conf);
 	}
 
 	@Override
 	public WorkUnit prepareSolve(final Configuration conf) {
 		final LoopyMutaGame game = (LoopyMutaGame) conf.getGame();
 		recordPool = new Pool<LoopyRecord>(new Factory<LoopyRecord>() {
 
 			public LoopyRecord newObject() {
 				return game.getRecord();
 			}
 
 			public void reset(LoopyRecord t) {
 				t.value = Value.UNDECIDED;
 			}
 
 		});
 		long hashSpace = game.numHashes();
 		Record defaultRecord = game.getRecord();
 		defaultRecord.value = Value.IMPOSSIBLE; //why do we set it to impossible?
 		writeDb.fill(conf.getGame().recordToLong(null, defaultRecord), 0,
 				hashSpace);
 
 		return new WorkUnit() {
 
 			public void conquer() {
 				solve(conf);
 			}
 
 			public List<WorkUnit> divide(int num) {
 				throw new UnsupportedOperationException();
 			}
 
 		};
 	}
 
 	public void solve(Configuration conf) {
 		LoopyMutaGame game = (LoopyMutaGame) conf.getGame();
 		for (int startNum = 0; startNum < game.numStartingPositions(); startNum++) {
 			game.setStartingPosition(startNum);
 			solve(game, game.getRecord(), 0, readDb.getHandle(),
 					writeDb.getHandle());
 		}
 		/*
 		 * Run through database:
 		 * 	If (database value)<DRAW and remainingChildren>0:
 		 * 		(database value)=DRAW
 		 */
 		
 		for (long i = 0; i < readDb.numRecords(); i++) {
 			//need to fill in 
			if 
 		}
 
 	}
 
 	private void solve(LoopyMutaGame game, LoopyRecord value, int depth,
 			DatabaseHandle readDh, DatabaseHandle writeDh) {
 /*
  * value = {retrieve from database}
  */
 	long hash = game.getHash();
 	game.longToRecord(readDb.getRecord(readDh, hash), value);
 /*
  *		case IMPOSSIBLE:
  *			value.value = primitiveValue()
  *			if primitive:
  *				value.remoteness = value.remainingChildren = 0
  *				{Store value in database}
  *				value = value.previousPosition()
  *				Run through parents:
  *					fix(..., false)
  */
 	switch(value.value){
 		case IMPOSSIBLE:
 			value.value = game.primitiveValue();
 			if (game.primitiveValue() != Value.UNDECIDED){
 				value.remoteness = 0;
 				value.remainingChildren = 0;
 				
 				writeDb.putRecord(writeDh, hash, game.recordToLong(value));
 				value.previousPosition();
 				//Run through parents:
 				
 				//not sure
 				while(value.remainingChildren >= 0) {
 					fix(game, value, depth, readDh, writeDh, false);
 				}
 			}
 /*	
  *			else:
  *				value.remainingChildren = len(children)
  *				value.value = DRAW
  *				{Store value in database}
  *				bestValue = -infinity
  *
  */
 			else{
 				//value.remainingChildren = len(children);
 				value.value = Value.DRAW;
 				writeDb.putRecord(writeDh, hash, game.recordToLong(value));
 				//bestValue = -infinity;
 				
				
 			}
 	}
 /*			Run through children:
  *					solve(...)
  *					if value.value == UNDECIDED:
  *						bestValue = {retrieve from database}
  *					else:
  *						if(value.remainingChildren==0):
  *							value.remainingChildren = (database value).remainingChildren - 1
  *						else
  *							value.remainingChildren = (database value).remainingChildren
  *						if(value>bestValue)
  *							bestValue = value
  *							{store value in database}
  *						else
  *							{store value.remainingChildren in database}
  *				value = bestValue
  *				Run through parents:
  *					fix(..., false)
  *			value = UNDECIDED
  *		default:
  *			value = value.previousPosition()
  *
 */
 	}
	private void fix(LoopyMutaGame game, LoopyRecord value, int depth,
 			DatabaseHandle readDh, DatabaseHandle writeDh, boolean update) {
 /*
  * (database value) = {retrieve from database}
 * Question: what's the difference between database value  and LoopyRecord value 
 */
		
		long hash = game.getHash();
		game.longToRecord(readDb.getRecord(readDh, hash), value);
		//how do we define database value?
/* 	case IMPOSSIBLE:
  * 		Do nothing
  * 	default:
  *  If update:
  *  	value.remainingChildren = (database value).remainingChildren
  *  else:
  *  	value.remainingChildren = (database value).remainingChildren - 1
 */
		switch (value.value){
			case IMPOSSIBLE:
				return;
			default:
			if (update){
				value.remainingChildren = 
					
			}
		}
		
		
/*  
  *  if (database value).value is DRAW or value>(database value)
  *  	{Store value in database}
  *  	value = value.previousPosition()
  *  	Run through parents:
  *  		fix(..., not (database value changed from <=DRAW to >DRAW or 
  *  				(database value<DRAW and database value.remainingChildren changed from 1 to 0)))
  *  	value = value.nextPosition()
  *  else
  *  	{Store value.remainingChildren in database}
  */
 	}
 }
