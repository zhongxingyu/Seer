 package phasebook.lotterybet;
 
 import java.sql.Timestamp;
 import javax.annotation.Generated;
 import javax.persistence.metamodel.SingularAttribute;
 import javax.persistence.metamodel.StaticMetamodel;
 
@Generated(value="Dali", date="2011-12-01T14:00:13.411+0000")
 @StaticMetamodel(LotteryBet.class)
 public class LotteryBet_ {
 	public static volatile SingularAttribute<LotteryBet, Integer> id;
 	public static volatile SingularAttribute<LotteryBet, Float> betValue;
 	public static volatile SingularAttribute<LotteryBet, Integer> betNumber;
 	public static volatile SingularAttribute<LotteryBet, Float> valueWon;
 	public static volatile SingularAttribute<LotteryBet, Timestamp> createdAT;
 	public static volatile SingularAttribute<LotteryBet, Boolean> read_;
	public static volatile SingularAttribute<LotteryBet, Integer> userId;
 	public static volatile SingularAttribute<LotteryBet, Integer> lotteryId;
 	public static volatile SingularAttribute<LotteryBet, Integer> lotteryNumber;
 	public static volatile SingularAttribute<LotteryBet, Timestamp> lotteryDate;
 }
