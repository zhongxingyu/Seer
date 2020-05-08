 package Estate;
 
 import Player.Position;
 
 public class Buy extends Deal {
     public Buy(EstateMap estateMap, Bank bank) {
         super(estateMap, bank);
     }
 
     public void handle(Position position, String roleName) {
        if (isAgree("是否花费" + estateMap.get(position).basePrice + "元购买该地产?")) {
             estateMap.update(position, roleName);
             bank.withdraw(roleName, estateMap.get(position).basePrice);
         }
     }
 }
