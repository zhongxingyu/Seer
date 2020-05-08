 package Estate;
 
 import Player.Position;
 
 public class Update extends Deal {
     public Update(EstateMap estateMap, Bank bank) {
         super(estateMap, bank);
     }
 
     public void handle(Position position, String roleName) {
        if (isAgree("是否花费" + estateMap.get(position).basePrice + "元升级该地产?")) {
             estateMap.update(position, roleName);
             bank.withdraw(estateMap.get(position).owner, estateMap.get(position).basePrice);
         }
     }
 }
