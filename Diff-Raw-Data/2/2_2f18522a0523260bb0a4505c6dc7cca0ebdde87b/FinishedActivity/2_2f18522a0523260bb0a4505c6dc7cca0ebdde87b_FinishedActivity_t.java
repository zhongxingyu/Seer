 package com.geeksong.agricolascorer;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import com.geeksong.agricolascorer.mapper.SavedGameMapper;
 import com.geeksong.agricolascorer.model.AgricolaScore;
 import com.geeksong.agricolascorer.model.AllCreaturesScore;
 import com.geeksong.agricolascorer.model.GameType;
 import com.geeksong.agricolascorer.model.Score;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 // SuppressWarnings: for generic around Score, because Score is not parameterized outside this class
 @SuppressWarnings("unchecked")
 public class FinishedActivity extends Activity {
     private static final Map<GameType, GameTypeHandler> GAME_TYPE_HANDLERS = new HashMap<GameType, GameTypeHandler>();
     static {
         GAME_TYPE_HANDLERS.put(GameType.Agricola, new AgricolaHandler());
         GAME_TYPE_HANDLERS.put(GameType.Farmers, new FarmersHandler());
         GAME_TYPE_HANDLERS.put(GameType.AllCreatures, new AllCreaturesHandler());
     }
 
     private SavedGameMapper mapper;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_finished);
 
         mapper = new SavedGameMapper();
 
         ArrayList<Score> scores = GameCache.getInstance().getScoreList();
         GameType gameType = GameCache.getInstance().getGameType();
         GameTypeHandler gameTypeHandler = GAME_TYPE_HANDLERS.get(gameType);
 
         createHeaderRow(scores);
         createBodyRows(gameTypeHandler, scores);
         createFooterRow(scores);
     }
 
     private void createHeaderRow(ArrayList<Score> scores) {
         TableRow tableRow = (TableRow) findViewById(R.id.finishedTableHeader);
         for (Score score : scores) {
             tableRow.addView(createPlayerNameCell(score), createTableCellLayout());
         }
     }
 
     private void createBodyRows(GameTypeHandler gameTypeHandler, ArrayList<Score> scores) {
         TableLayout table = (TableLayout) findViewById(R.id.finishedTable);
 
         int[] scoreLabelIds = gameTypeHandler.getScoreLabelIds();
         for (int i = 0; i < scoreLabelIds.length; i++) {
             int scoreLabelId = scoreLabelIds[i];
             TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.finished_row, null);
             ((TextView) tableRow.getChildAt(0)).setText(scoreLabelId);
 
             // Detect min/max
             int maxUnitScore = Integer.MIN_VALUE;
             int minUnitScore = Integer.MAX_VALUE;
             for (Score score : scores) {
                 int unitScore = gameTypeHandler.getUnitScoreForRow(score, i);
                 if (unitScore > maxUnitScore)
                     maxUnitScore = unitScore;
                 if (unitScore < minUnitScore)
                     minUnitScore = unitScore;
             }
 
             for (Score score : scores) {
                 tableRow.addView(createUnitScoreCell(gameTypeHandler, i, score, minUnitScore, maxUnitScore),
                         createTableCellLayout());
             }
 
             table.addView(tableRow, table.getChildCount() - 1); // Before footer
         }
     }
 
     private void createFooterRow(ArrayList<Score> scores) {
         TableRow tableRow;
         tableRow = (TableRow) findViewById(R.id.finishedTableFooter);
 
         // Detect min/max
         int maxTotalScore = Integer.MIN_VALUE;
         int minTotalScore = Integer.MAX_VALUE;
         for (Score score : scores) {
             int totalScore = score.getTotalScore();
             if (totalScore > maxTotalScore)
                 maxTotalScore = totalScore;
             if (totalScore < minTotalScore)
                 minTotalScore = totalScore;
         }
 
         for (Score score : scores) {
             tableRow.addView(createTotalScoreCell(score, minTotalScore, maxTotalScore),
                     createTableCellLayout());
         }
     }
 
     private TableRow.LayoutParams createTableCellLayout() {
         return new TableRow.LayoutParams(0,
                 ViewGroup.LayoutParams.WRAP_CONTENT, 1);
     }
 
     private View createPlayerNameCell(Score score) {
         TextView textView = (TextView) getLayoutInflater().inflate(R.layout.finished_cell_player, null);
         textView.setText(score.getPlayer().getName());
 
         return textView;
     }
 
     private View createUnitScoreCell(GameTypeHandler gameTypeHandler, int scoreIndex, Score score, int minUnitScore,
                                      int maxUnitScore) {
         int unitScore = gameTypeHandler.getUnitScoreForRow(score, scoreIndex);
 
         int layout;
         if ((unitScore == maxUnitScore) && (unitScore != minUnitScore))
             layout = R.layout.finished_cell_unit_score_max;
         else if ((unitScore == minUnitScore) && (unitScore != maxUnitScore))
             layout = R.layout.finished_cell_unit_score_min;
         else
             layout = R.layout.finished_cell_unit_score;
 
         TextView textView = (TextView) getLayoutInflater().inflate(layout, null);
         textView.setText(String.valueOf(unitScore));
 
         return textView;
     }
 
     private View createTotalScoreCell(Score score, int minTotalScore, int maxTotalScore) {
         int totalScore = score.getTotalScore();
         int layout;
         if ((totalScore == maxTotalScore) && (totalScore != minTotalScore))
             layout = R.layout.finished_cell_total_score_max;
         else if ((totalScore == minTotalScore) && (totalScore != maxTotalScore))
             layout = R.layout.finished_cell_total_score_min;
         else
             layout = R.layout.finished_cell_total_score;
 
         TextView textView = (TextView) getLayoutInflater().inflate(layout, null);
         textView.setText(String.valueOf(totalScore));
 
         return textView;
     }
 
     public void saveGame(View source) {
         GameCache cache = GameCache.getInstance();
         if (cache.isFromDatabase())
             mapper.save(cache.getGame());
         else
             mapper.save(cache.getScoreList(), GameCache.getInstance().getGameType());
 
         startAgain(source);
     }
 
     public void startAgain(View source) {
         GameCache.getInstance().clearGame();
 
         Intent createGameIntent = new Intent(source.getContext(), CreateGameActivity.class);
         startActivity(createGameIntent);
     }
 
     private static interface GameTypeHandler<T extends Score> {
         int[] getScoreLabelIds();
         int getUnitScoreForRow(T score, int rowNum);
     }
 
     private static class AgricolaHandler implements GameTypeHandler<AgricolaScore> {
 
         public static final int[] SCORE_LABEL_IDS = new int[]{
                 R.string.fields_label,
                 R.string.pastures_label,
                 R.string.grain_label,
                 R.string.vegetables_label,
                 R.string.sheep_label,
                 R.string.wild_boar_label,
                 R.string.cattle_label,
                 R.string.family_members_label,
                 R.string.rooms_label,
                 R.string.unused_spaces_label,
                 R.string.points_for_cards_label,
                 R.string.fenced_stables_label,
                 R.string.bonus_points_label,
                 R.string.begging_cards_label
         };
 
         @Override
         public int[] getScoreLabelIds() {
             return SCORE_LABEL_IDS;
         }
 
         @Override
         public int getUnitScoreForRow(AgricolaScore score, int scoreIndex) {
             switch (scoreIndex) {
                 case 0:
                     return score.getFieldScore();
                 case 1:
                     return score.getPastureScore();
                 case 2:
                     return score.getGrainScore();
                 case 3:
                     return score.getVegetableScore();
                 case 4:
                     return score.getSheepScore();
                 case 5:
                     return score.getBoarScore();
                 case 6:
                     return score.getCattleScore();
                 case 7:
                     return score.getFamilyMemberScore();
                 case 8:
                     return score.getRoomsScore();
                 case 9:
                     return score.getUnusedSpacesScore();
                 case 10:
                     return score.getPointsForCards();
                 case 11:
                     return score.getFencedStablesScore();
                 case 12:
                     return score.getBonusPoints();
                 case 13:
                     return score.getBeggingCardsScore();
                 default:
                     throw new IllegalArgumentException("No score defined at row " + scoreIndex + "!?");
             }
         }
     }
 
     private static class FarmersHandler extends AgricolaHandler {
 
         public static final int[] SCORE_LABEL_IDS = new int[AgricolaHandler.SCORE_LABEL_IDS.length + 2];
         static {
            System.arraycopy(AgricolaHandler.SCORE_LABEL_IDS, 0, SCORE_LABEL_IDS, 0,
                    AgricolaHandler.SCORE_LABEL_IDS.length);
             SCORE_LABEL_IDS[AgricolaHandler.SCORE_LABEL_IDS.length] = R.string.horses_label;
             SCORE_LABEL_IDS[AgricolaHandler.SCORE_LABEL_IDS.length + 1] = R.string.in_bed_family_label;
         }
 
         @Override
         public int[] getScoreLabelIds() {
             return SCORE_LABEL_IDS;
         }
 
         @Override
         public int getUnitScoreForRow(AgricolaScore score, int scoreIndex) {
             switch (scoreIndex) {
                 case 14:
                     return score.getHorsesScore();
                 case 15:
                     return score.getInBedFamilyCount();
                 default:
                     return super.getUnitScoreForRow(score, scoreIndex);
             }
         }
     }
 
     private static class AllCreaturesHandler implements GameTypeHandler<AllCreaturesScore> {
 
         public static final int[] SCORE_LABEL_IDS = new int[]{
                 R.string.sheep_label,
                 R.string.wild_boar_label,
                 R.string.cattle_label,
                 R.string.horses_label,
                 R.string.full_expansion_count_label,
                 R.string.building_score_label
         };
 
         @Override
         public int[] getScoreLabelIds() {
             return SCORE_LABEL_IDS;
         }
 
         @Override
         public int getUnitScoreForRow(AllCreaturesScore score, int scoreIndex) {
             switch (scoreIndex) {
                 case 0:
                     return score.getSheepScore();
                 case 1:
                     return score.getWildBoarScore();
                 case 2:
                     return score.getCattleScore();
                 case 3:
                     return score.getHorseScore();
                 case 4:
                     return score.getFullExpansionScore();
                 case 5:
                     return score.getBuildingScore();
                 default:
                     throw new IllegalArgumentException("No score defined at row " + scoreIndex + "!?");
             }
         }
     }
 }
