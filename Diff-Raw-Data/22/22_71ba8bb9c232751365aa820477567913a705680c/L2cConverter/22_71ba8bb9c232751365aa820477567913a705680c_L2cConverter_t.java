 package jp.dev7.enchant.doga.converter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.vecmath.Matrix4d;
 
 import jp.dev7.enchant.doga.converter.data.EnchantArticulated;
 import jp.dev7.enchant.doga.converter.data.EnchantMesh;
 import jp.dev7.enchant.doga.converter.data.EnchantPose;
 import jp.dev7.enchant.doga.converter.data.EnchantUnit;
 import jp.dev7.enchant.doga.parser.L2cFileParser;
 import jp.dev7.enchant.doga.parser.PoseL2cLineParser;
 import jp.dev7.enchant.doga.parser.data.Connection;
 import jp.dev7.enchant.doga.parser.data.ConnectionObj;
 import jp.dev7.enchant.doga.parser.data.Pose;
 import jp.dev7.enchant.doga.util.Utils;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class L2cConverter extends L3cConverter {
 
     private final static Logger LOG = LoggerFactory
             .getLogger(L2cConverter.class);
 
     @Override
     public EnchantArticulated convert(File l2cFile) throws Exception {
         // L2Cファイルをパース
         final L2cFileParser p = new L2cFileParser();
         final Connection l2c = p.parse(l2cFile);
 
         // ポーズデータの一時保存用
         final Map<String, List<double[]>> tempPoseMap = Maps.newHashMap();
 
         // モデルデータ
         final ConnectionObj root = l2c.getRootUnit();
         final File baseDir = l2cFile.getParentFile();
         final UnitConverter unitConverter = new UnitConverter();
         final List<double[]> initialPose = Lists.newArrayList();
         final EnchantUnit model = convert(root, unitConverter, baseDir,
                 initialPose);
 
         final EnchantArticulated result = new EnchantArticulated();
         result.setRoot(model);
 
         // 初期ポーズ
         LOG.info("_initialPoseユニット数=" + initialPose.size());
         tempPoseMap.put("_initialPose", initialPose);
 
         // ポーズデータ
         final BufferedReader in = new BufferedReader(new InputStreamReader(
                 new FileInputStream(l2cFile), "Shift_JIS"));
         String line;
         boolean start = false;
         while ((line = in.readLine()) != null) {
             line = line.trim();
             if (line.equals("Pose:")) {
                 start = true;
             } else if (start) {
                 try {
                     final Pose pose = PoseL2cLineParser.parse(line);
                    LOG.info("Pose [" + pose.getName() + "] ユニット数="
                             + pose.getUnitPose().size());

                    final List<double[]> basePoses = Lists.transform(
                            pose.getUnitPose(), poseTransformFunc);

                    // ルートユニットの位置変化を補正
                    double[] rootUnitPose = basePoses.get(0);
                    rootUnitPose[3] -= model.getBasePosition()[0];
                    rootUnitPose[4] -= model.getBasePosition()[1];
                    rootUnitPose[5] -= model.getBasePosition()[2];

                    final List<double[]> ajustPoses = Lists.newArrayList();
                    ajustPoses.add(rootUnitPose);
                    for (int i = 1; i < basePoses.size(); i++) {
                        ajustPoses.add(basePoses.get(i));
                    }

                    tempPoseMap.put(pose.getName(), ajustPoses);
                 } catch (Exception e) {
                     if (line.substring(0, 1).matches("[0-9]")) {
                         LOG.error("ポーズデータのパースに失敗 [" + line + "]", e);
                         throw e;
                     }
                     break;
                 }
             }
         }
 
         // ポーズデータのツリー化
         for (String poseName : tempPoseMap.keySet()) {
             final Iterator<double[]> pIte = tempPoseMap.get(poseName)
                     .iterator();
             final EnchantPose pose = new EnchantPose();
             pose.setRoot(buildPoseTree(result.getRoot(), pIte));
             pose.setName(poseName);
 
             result.getPoses().add(pose);
         }
 
         return result;
     }
 
     private EnchantUnit convert(ConnectionObj unit,
             UnitConverter unitConverter, File baseDir,
             List<double[]> initailPose) throws Exception {
         final EnchantUnit result = new EnchantUnit();
 
         final List<EnchantMesh> l2p;
         final File file = l2pFileName(unit, baseDir);
         if (!file.exists()) {
             l2p = Lists.newArrayList();
             LOG.warn("L2Pファイルがない！ " + unit.getUnitFileName() + ", "
                     + file.getAbsolutePath());
         } else {
             final Matrix4d xform = Utils.getIdentity();
             xform.mul(scale(unit));
             xform.mul(mov(unit));
             l2p = unitConverter.convert(file, xform);
         }
 
         result.setL3p(l2p);
         result.setBasePosition(basePosition(unit));
         initailPose.add(initialPose(unit));
 
         for (ConnectionObj child : unit.getChildUnits()) {
             result.getChildUnits().add(
                     convert(child, unitConverter, baseDir, initailPose));
         }
 
         return result;
     }
 
     private File l2pFileName(ConnectionObj unit, File baseDir)
             throws IOException {
         return Utils.findL2pFile(unit.getUnitFileName(), baseDir);
     }
 
 }
