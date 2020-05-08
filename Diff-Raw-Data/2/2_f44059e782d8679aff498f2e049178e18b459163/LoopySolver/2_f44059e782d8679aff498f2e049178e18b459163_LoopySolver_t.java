 package edu.berkeley.gamesman.solver;
 
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
 		defaultRecord.value = Value.IMPOSSIBLE;
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
 	}
 
 	private void solve(LoopyMutaGame game, Record value, int depth,
 			DatabaseHandle readDh, DatabaseHandle writeDh) {
 /*
  * value = {retrieve from database}
  *		case IMPOSSIBLE:
  *			value.value = primitiveValue()
  *			if primitive:
  *				value.remoteness = value.remainingChildren = 0
  *				{Store value in database}
  *				value = value.previousPosition()
  *				Run through parents:
  *					fix(..., false)
  *			else:
  *				value.remainingChildren = len(children)
  *				value.value = DRAW
  *				{Store value in database}
  *				bestValue = -infinity
  *				Run through children:
  *					solve(...)
  *					if value.value == UNDECIDED:
  *						bestValue = {retrieve from database}
  *					else:
 *						if(value.remainingChildren==0 OR value.value.nextPosition() > DRAW):
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
 
 	private void fix(LoopyMutaGame game, Record value, int depth,
 			DatabaseHandle readDh, DatabaseHandle writeDh, boolean update) {
 /*
  * (database value) = {retrieve from database}
  * 	case IMPOSSIBLE:
  * 		Do nothing
  * 	default:
  *  If update:
  *  	value.remainingChildren = (database value).remainingChildren
  *  else:
  *  	value.remainingChildren = (database value).remainingChildren - 1
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
