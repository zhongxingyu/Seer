 public abstract class Gift {
     abstract void open(Player player);
 
     public static Gift makeGift(int giftCode){
         switch (giftCode) {
             case MoneyGift.MONEY_GIFT_CODE:
                 return new MoneyGift();
             case PointGift.POINT_GIFT_CODE:
                 return new PointGift();
             case GodOfLuckGift.GOD_OF_LUCK_GIFT_CODE:
                 return new GodOfLuckGift();
         }

        throw new InvalidGiftException();
     }
 }
