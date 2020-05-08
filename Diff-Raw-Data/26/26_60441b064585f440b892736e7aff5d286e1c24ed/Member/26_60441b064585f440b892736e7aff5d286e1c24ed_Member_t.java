 package models;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import models.enums.Skill;
 import models.enums.Team;
 import play.db.jpa.GenericModel;
 
 import javax.persistence.*;
 import java.util.List;
 import java.util.Set;
 
 @Entity(name = "member")
 public class Member extends GenericModel {
 
     @Id
     @GeneratedValue
     public Long memberId;
     @ManyToOne(cascade = CascadeType.DETACH)
     public Village village; // 村
     @ManyToOne(cascade = CascadeType.DETACH)
     public User user; // 中身
     public String name;
     @ManyToOne(cascade = CascadeType.DETACH)
     public Chara chara; // 使用キャラ
     public Skill skill = Skill.Villager; // 所持能力
     public Long targetMemberId = 0L; // 能力行使対象
     public Long targetMemberId2 = 0L; // 能力行使対象
     public Team team = Team.Village; // 所属陣営(恋すると書き換わる)
     public boolean alive = true;
 
     public static List<Member> findByVillage(Village village) {
         return find("village = ?1", village).fetch();
     }
 
     public static List<Member> findAlive(Village village) {
         return find("village = ?1 and alive = ?2", village, true).fetch();
     }
 
     public static List<Member> findByVillageExcludeMe(Village village, Member me) {
         if (me == null) return Lists.newArrayList();
         return find("village = ?1 and memberId != ?2", village, me.memberId).fetch();
     }
 
     public static int countByVillage(Village village) {
         return Long.valueOf(count("village = ?1", village)).intValue();
     }
 
     public static boolean exist(Village village, User user) {
         return count("village = ?1 and user = ?2", village, user) > 0L;
     }
 
     public static Member findByIds(Village village, User user) {
        if (user == null) {
            return find("village = ?1 and user is null", village).first();
        }
         return find("village = ?1 and user = ?2", village, user).first();
     }
 
     public static Member findByIds(Village village, Long memberId) {
         return find("village = ?1 and memberId = ?2", village, memberId).first();
     }
 
     private static String uniqueName(Village village, Chara chara) {
         List<Member> members = findByVillage(village);
         String prefix = chara.name;
         Set<String> names = Sets.newHashSet();
         for (Member m : members) {
             names.add(m.name);
         }
         if (!names.contains(prefix))
             return prefix;
         int length = members.size() + 1;
         for (int i = 2; i < length; i++) {
             if (!names.contains(prefix + i))
                 return prefix + i;
         }
         return prefix + length;
     }
 
     public static Member enter(Village village, User user, Chara chara) {
         if (!exist(village, user)) {
             Member m = new Member();
             m.village = village;
             m.chara = chara;
             m.user = user;
             m.skill = Skill.Villager;
             m.name = uniqueName(village, chara);
             return m.save();
         }
         return null;
     }
 
     public static Member leave(Village village, User user) {
         Member m = findByIds(village, user);
         if (m == null) return null;
         return m.delete();
     }
 
 
     // 生存状態の更新、参照系
 
     /**
      * 生存しているか
      *
      * @return 生きていれば<code>true</code>
      */
     public boolean isAlive() {
         return alive;
     }
 
     /**
      * 殺す
      *
      * @return 処理が成功すれば<code>true</code>
      */
     private boolean kill() {
         if (!isAlive()) return false;
         alive = false;
         return validateAndSave();
     }
 
     /**
      * 噛む
      *
      * @return 処理が成功すれば<code>true</code>
      */
     public boolean attack() {
         return kill();
     }
 
     /**
      * 吊る
      *
      * @return 処理が成功すれば<code>true</code>
      */
     public boolean execute() {
         return kill();
     }
 
     /**
      * 能力選択、投票の有無
      *
      * @return コミット可能なら<code>true</code>
      */
     public boolean isCommitable() {
         return targetMemberId > 0L;
     }
 
     /**
      * 能力や投票の対象を選ぶ
      *
      * @param tgt1 第一目標
      * @param tgt2 第二目標
      * @return 処理が成功すれば<code>true</code>
      */
     public boolean setTarget(Long tgt1, Long tgt2) {
         targetMemberId = tgt1;
         targetMemberId2 = tgt2;
         return validateAndSave();
     }
 
     /**
      * 投票する
      *
      * @param target 投票先
      * @return 処理が成功すれば<code>true</code>
      */
     public boolean vote(Long target) {
         return setTarget(target, null);
     }
 
     /**
      * 能力や投票の対象をリセットする
      *
      * @return 処理が成功すれば<code>true</code>
      */
     public boolean resetTarget() {
         return setTarget(-1L, -1L);
     }
 
 }
