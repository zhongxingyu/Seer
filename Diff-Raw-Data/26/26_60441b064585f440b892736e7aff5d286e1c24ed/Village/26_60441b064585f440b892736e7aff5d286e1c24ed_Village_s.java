 package models;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Objects;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import consts.Constants;
 import models.enums.Permission;
 import models.enums.Skill;
 import models.enums.State;
 import models.enums.Team;
 import org.joda.time.DateTime;
 import play.db.jpa.GenericModel;
 import utils.CommitUtil;
 import utils.MemberUtil;
 import utils.SkillUtils;
 
 import javax.persistence.*;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 @Entity(name = "village")
 public class Village extends GenericModel {
 
     @Id
     @GeneratedValue
     public Long villageId;
     public Long userId;
     @OneToMany(cascade = CascadeType.DETACH, mappedBy = "village")
     public List<Member> members;
     // 設定
     public String name; //村名
     @Lob
     public String form = Constants.FORM_CATTLE; //編成表
     public int dayTime = 5; // 日中の時間
     public int nightTime = 3; // 夜の時間
     public Long dummyMemberId = null;
     public int maxMember;
     public int minMember;
     // 進行状況
     public State state = State.Prologue;
     public int dayCount = 1;
     public Date nextCommit = null;
     // 結果
     public Team winner = Team.Others; // 勝者不定
 
     public static Village findById(Long villageId) {
         return find("villageId = ?1", villageId).first();
     }
 
     public static Village findByIdAndAdmin(Long villageId, Long userId) {
         return find("villageId = ?1 and userId = ?2", villageId, userId).first();
     }
 
     //public static List<Village> findByState(State state) {
     //    return find("state = ?1", state).fetch();
     //}
 
     /**
      * 村建て&例のシステムメッセージ
      *
      * @param user      管理者
      * @param name      村名
      * @param form      ワイドカスタム構成
      * @param dayTime   昼間時間
      * @param nightTime 夜時間
      * @param dummy     ダミーを含むかどうか
      * @return 立った村
      */
     public static Village settle(User user, String name, String form, int dayTime, int nightTime, boolean dummy) {
         Village v = new Village();
         v.userId = user.userId;
         v.name = name;
         v.form = form;
         v.dayTime = dayTime;
         v.nightTime = nightTime;
         if (!v.parseOption()) return null;
         v = v.save();
         if (v == null) return null;
         // 人狼、それは
         Res.createNewSystemMessage(v, Permission.Public, Skill.Dummy, Constants.VILLAGE_SETTLE);
         // ダミーの入村
         if (dummy) {
             v.enterDummy();
             v = v.save();
         }
         return v;
     }
 
     /**
      * 村設定変更
      *
      * @param name      村名
      * @param form      ワイドカスタム構成
      * @param dayTime   昼間時間
      * @param nightTime 夜時間
      * @param dummy     ダミーを含むかどうか
      * @return 更新された村
      */
     public Village updateVillage(String name, String form, int dayTime, int nightTime, boolean dummy) {
         this.name = name;
         this.form = form;
         this.dayTime = dayTime;
         this.nightTime = nightTime;
         if (!this.parseOption()) return null;
         // ダミーの入退村
         if (dummy) {
             enterDummy();
         } else {
             leaveDummy();
         }
         return save();
     }
 
     /**
      * ダミーの存在確認
      *
      * @return ダミーがいれば<code>true</code>
      */
     public boolean hasDummy() {
         return dummyMemberId != null;
     }
 
     /**
      * ダミーがいなければ追加し、メンバーIDを返す
      *
      * @return メンバーID
      */
     private Long enterDummy() {
         if (hasDummy()) return dummyMemberId;
         if (!canEnter()) return null;
         Member m = enter(null, Chara.dummy());
         if (m == null) return null;
         dummyMemberId = m.memberId;
         return m.memberId;
     }
 
     /**
      * ダミーの退村
      */
     public void leaveDummy() {
         if (!canLeave() || !hasDummy()) return;
        Member m = Member.leave(this, null);
        if (m == null) return;
         dummyMemberId = null;
     }
 
     /**
      * ワイドカスタム構成のパース
      *
      * @return 成功すれば<code>true</code>
      */
     private boolean parseOption() {
         try {
             Map<Integer, List<Skill>> map = SkillUtils.getSkillsMapFromFormStr(form);
             maxMember = 0;
             minMember = Integer.MAX_VALUE;
             for (Integer n : map.keySet()) {
                 maxMember = Math.max(maxMember, n);
                 minMember = Math.min(minMember, n);
             }
         } catch (IllegalArgumentException e) {
             return false;
         }
         return true;
     }
 
 
     /**
      * 村開始。対応する編成があれば役職を割り振り、初日夜へ
      *
      * @return 開始できれば<code>true</code>
      */
     public boolean start() {
         if (state != State.Prologue)
             return false;
         int memberCount = countMember();
         Map<Integer, List<Skill>> map = SkillUtils.getSkillsMapFromFormStr(form);
         if (!map.containsKey(memberCount))
             return false;
         List<Skill> skills = map.get(memberCount);
         List<Member> members = Member.findByVillage(this);
         MemberUtil.setSkill(skills, members, dummyMemberId);
         Map<Skill, Set<Member>> work = MemberUtil.skillMembers(members);
         List<String> countMessages = Lists.newArrayList();
         for (Skill s : Skill.values()) {
             int count = work.get(s).size();
             if (count == 0) continue;
             countMessages.add(s.getLabel() + "が" + count + "人");
         }
         Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, String.format(Constants.VILLAGE_COUNT, Joiner.on("、").join(countMessages)));
         state = State.Night;
         nextCommit = DateTime.now().plusMinutes(nightTime).toDate();
         for (Member m : members) {
             Res.createNewPersonalMessage(this, m, Permission.Personal, m.skill, String.format(Constants.SKILL_SET, m.name, m.skill.getLabel()));
         }
         Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, Constants.TWILIGHT);
         return save() != null;
     }
 
     /**
      * 村状態コミットのハンドラ
      * 更新時に全員が行動対象を選択していればコミットされる
      * 時間切れなら行動対象を自分以外にして強制的にコミット
      *
      * @return 成功すれば<code>true</code>
      */
     public boolean tryCommit() {
         if (!isRunning()) return false;
         boolean force = nextCommit != null && nextCommit.before(new Date(System.currentTimeMillis()));
         List<Member> members = Member.findAlive(this);
         if (!force) {
             for (Member m : members) {
                 if (!m.isCommitable() && (m.skill.hasAbility() || state == State.Day)) return false;
             }
         }
         boolean success = false;
         if (state == State.Day) {
             success = commitToNight(members);
         } else if (state == State.Night) {
             success = commitToDay(members);
         }
         return success;
     }
 
 
     /**
      * 勝利判定
      *
      * @param members アクティブなメンバーリスト(一人死んでいる可能性がある)
      * @return 決着ついたかどうか
      */
     private boolean endCheck(List<Member> members) {
         int human = 0;
         int wolf = 0;
         for (Member m : members) {
             if (m.isAlive()) {
                 if (m.skill == Skill.Werewolf) {
                     wolf++;
                 } else {
                     human++;
                 }
             }
         }
         if (wolf == 0) {
             //村勝ち
             winner = Team.Village;
             Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, Constants.WIN_VILLAGER);
             state = State.Epilogue;
             nextCommit = null;
             return true;
         }
         if (human <= wolf) {
             // 狼勝ち
             winner = Team.Wolf;
             Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, Constants.WIN_WOLF);
             state = State.Epilogue;
             nextCommit = null;
             return true;
         }
         return false;
     }
 
     /**
      * 投票終了時、夜へ
      *
      * @return 成功すれば<code>true</code>
      */
     private boolean commitToNight(List<Member> members) {
         if (state != State.Day) return false;
         // 投票の集計
         Map<Long, Member> names = MemberUtil.memberMap(members); // id -> object
         Set<Long> memberIds = names.keySet();
 
         // 処刑
         List<String> voteMessages = Lists.newArrayList();
         for (Member m : members) {
             Long id = m.isCommitable() ? m.targetMemberId : CommitUtil.randomMemberId(memberIds, m.memberId);
             voteMessages.add(String.format(Constants.VOTE_ACTION, m.name, names.get(id).name) + (m.isCommitable() ? "" : Constants.RANDOM));
             m.targetMemberId = id;
         }
         Map<Long, Integer> votes = CommitUtil.vote(Sets.newHashSet(members), memberIds, false); // id -> 票数
         //for (Long memberId : votes.keySet()) Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, String.format(Constants.VOTE_COUNT, names.get(memberId).name, votes.get(memberId)));
         // 処刑対象の決定
         Long inmateId = CommitUtil.getElected(memberIds, votes);
         Member inmate = names.get(inmateId);
         // 処刑メッセージと処刑
         Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, Joiner.on("\n").join(voteMessages) + "\n\n" + String.format(Constants.EXECUTION_ACTION, inmate.name));
         inmate.execute();
         // 霊メッセージ
         Res.createNewSystemMessage(this, Permission.Group, Skill.Mystic, String.format(Constants.EXECUTION_MYSTIC, inmate.name, inmate.skill.getAppearance()));
         // 選択された対象のリセット
         CommitUtil.resetTargets(members);
         if (!endCheck(members)) {
             // 無事なら続行
             state = State.Night;
             nextCommit = DateTime.now().plusMinutes(nightTime).toDate();
             Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, Constants.TWILIGHT);
         }
         return save() != null;
     }
 
     /**
      * 能力発動と夜明け
      *
      * @return 成功すれば<code>true</code>
      */
     private boolean commitToDay(List<Member> members) {
         if (state != State.Night) return false;
         Map<Skill, Set<Member>> work = MemberUtil.skillMembers(members);
         Map<Long, Member> names = MemberUtil.memberMap(members); // id -> object
         // 狼：襲撃先の選定
         Long victimId = CommitUtil.processAttack(work.get(Skill.Werewolf), names.keySet(), dummyMemberId, dayCount);
         // 夜明け
         dayCount++;
         state = State.Day;
         nextCommit = DateTime.now().plusMinutes(dayTime).toDate();
         // 占い結果
         for (Member m : work.get(Skill.Augur)) {
             Member target = Objects.firstNonNull(names.get(m.targetMemberId), names.get(CommitUtil.randomMemberId(names.keySet(), m.memberId)));
             Res.createNewPersonalMessage(this, m, Permission.Personal, m.skill, String.format(Constants.FORTUNE_ACTION, target.name, target.skill.getAppearance()) + (m.isCommitable() ? "" : Constants.RANDOM));
         }
         // 護衛
         Set<Long> guardIds = Sets.newHashSet();
         if (dayCount > 2) { // 働くのは3日目夜明けより
             for (Member m : work.get(Skill.Hunter)) {
                 Member target = Objects.firstNonNull(names.get(m.targetMemberId), names.get(CommitUtil.randomMemberId(names.keySet(), m.memberId)));
                 guardIds.add(target.memberId);
                 Res.createNewPersonalMessage(this, m, Permission.Personal, m.skill, String.format(Constants.GUARD_ACTION, target.name) + (m.isCommitable() ? "" : Constants.RANDOM));
             }
         }
         // 襲撃/無残メッセージと襲撃
         Member victim = names.get(victimId);
         Res.createNewSystemMessage(this, Permission.Group, Skill.Werewolf, String.format(Constants.ATTACK_SET, victim.name));
         if (guardIds.contains(victimId)) { // GJ
             Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, Constants.ATTACK_FAILED);
         } else { // 護衛失敗
             Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, String.format(Constants.ATTACK_ACTION, victim.name));
             victim.attack();
         }
         // 選択された対象のリセット
         CommitUtil.resetTargets(members);
         endCheck(members);
         return save() != null;
     }
 
     // ユーザーの行動
 
     /**
      * ユーザーの入村
      *
      * @param user      ユーザー
      * @param chara キャラクター
      * @return メンバー
      */
     public Member enter(User user, Chara chara) {
         if (!canEnter()) return null;
         Member m = Member.enter(this, user, chara);
         if (m == null) return null;
         Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, String.format(Constants.VILLAGE_ENTER, m.name));
         return m;
     }
 
     /**
      * ユーザーの退村
      *
      * @param user ユーザー
      * @return メンバー
      */
     public Member leave(User user) {
         if (!canLeave()) return null;
         Member m = Member.leave(this, user);
         if (m == null) return null;
         Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, String.format(Constants.VILLAGE_LEAVE, m.name));
         return m;
     }
 
     /**
      * ユーザーの存在確認
      *
      * @param user ユーザー
      * @return いれば<code>true</code>
      */
     public boolean exist(User user) {
         return Member.exist(this, user);
     }
 
     /**
      * 能力設定/投票
      *
      * @param user     ユーザー
      * @param firstId  対象1
      * @param secondId 対象2
      * @return 成功すれば<code>true</code>
      */
     public boolean setTarget(User user, Long firstId, Long secondId) {
         if (user == null || firstId == null || !isRunning()) {
             return false;
         }
         Member me = Member.findByIds(this, user);
         Member first = Member.findByIds(this, firstId);
         if (me == null || first == null || !me.isAlive() || !first.isAlive()) {
             return false;
         }
         me.targetMemberId = first.memberId;
         if (secondId != null) {
             Member second = Member.findByIds(this, secondId);
             if (second != null && second.isAlive())
                 me.targetMemberId2 = second.memberId;
             // if (state == State.Night)  // message cupid;
         } else {
             if (state == State.Day) {
                 // 投票内容
                 Res.createNewPersonalMessage(this, me, Permission.Personal, Skill.Dummy, String.format(Constants.VOTE_SET, me.name, first.name));
             } else {
                 // 能力の行使内容
                 Res.createNewPersonalMessage(this, me, Permission.Personal, me.skill, String.format(Constants.ACTION_MESSAGE.get(me.skill), me.name, first.name));
             }
         }
 
         return me.save() != null;
     }
 
 
     // プロパティ確認系
 
     /**
      * 村の管理権限が対象ユーザーにあるかを確認
      *
      * @param user ユーザー
      * @return 管理権限の有無
      */
     public boolean isYours(User user) {
         return user != null && userId.equals(user.userId);
     }
 
     /**
      * @return 入村可能なら<code>true</code>
      */
     public boolean canEnter() {
         return state == State.Prologue && countMember() < maxMember;
     }
 
     /**
      * @return 入村者数
      */
     public int countMember() {
         return Member.countByVillage(this);
     }
 
     /**
      * @return 退村可能なら<code>true</code>
      */
     public boolean canLeave() {
         return state == State.Prologue;
     }
 
     /**
      * @return 村が進行中であれば<code>true</code>
      */
     public boolean isRunning() {
         return state == State.Day || state == State.Night;
     }
 
     /**
      * @return 村が決着済みであれば<code>true</code>
      */
     public boolean isFinished() {
         return state == State.Epilogue || state == State.Closed;
     }
 
     /**
      * (View)次回のコミット日時をUnixTime(ms)で返す
      *
      * @return コミット日時
      */
     public long getCommitTime() {
         if (nextCommit == null) return 0L;
         return nextCommit.getTime();
     }
 
 
 }
