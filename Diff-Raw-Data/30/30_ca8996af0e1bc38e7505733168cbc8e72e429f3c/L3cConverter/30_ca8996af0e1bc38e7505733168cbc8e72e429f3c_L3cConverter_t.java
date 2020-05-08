 package jp.dev7.enchant.doga.converter.l3c;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.vecmath.Matrix4d;
 import javax.vecmath.Quat4d;
 import javax.vecmath.Vector3d;
 
 import jp.dev7.enchant.doga.converter.EnchantArticulated;
 import jp.dev7.enchant.doga.converter.EnchantMesh;
 import jp.dev7.enchant.doga.converter.EnchantPose;
 import jp.dev7.enchant.doga.converter.EnchantPoseUnit;
 import jp.dev7.enchant.doga.converter.EnchantUnit;
 import jp.dev7.enchant.doga.converter.l3p.L3pConverter;
 import jp.dev7.enchant.doga.converter.suf.SufConverter;
 import jp.dev7.enchant.doga.parser.l3c.L3cFileParser;
 import jp.dev7.enchant.doga.parser.l3c.PoseLineParser;
 import jp.dev7.enchant.doga.parser.l3c.data.L3c;
 import jp.dev7.enchant.doga.parser.l3c.data.Pose;
 import jp.dev7.enchant.doga.parser.l3c.data.Unit;
 import jp.dev7.enchant.doga.util.Utils;
 import net.arnx.jsonic.JSON;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class L3cConverter {
 
 	private final Logger logger = LoggerFactory.getLogger(L3cConverter.class);
 
 	public String convertToJson(File l3cFile) throws Exception {
 		final StringWriter stringWriter = new StringWriter();
 		convertAndWriteJson(l3cFile, stringWriter);
 		return stringWriter.toString();
 	}
 
 	public void convertAndWriteJson(File l3cFile, Appendable writer)
 			throws Exception {
 		JSON.encode(convert(l3cFile), writer, false);
 	}
 
 	public EnchantArticulated convert(File l3cFile) throws Exception {
 		// L3Cファイルをパース
 		final L3cFileParser p = new L3cFileParser();
 		final L3c l3c = p.parse(l3cFile);
 
 		// ポーズデータの一時保存用
 		final Map<String, List<double[]>> tempPoseMap = Maps.newHashMap();
 
 		// モデルデータ
 		final Unit root = l3c.getRootUnit();
 		final File baseDir = l3cFile.getParentFile();
 		final L3pConverter l3pConverter = new L3pConverter();
 		final List<double[]> initialPose = Lists.newArrayList();
 		final EnchantUnit model = convert(root, l3pConverter, baseDir,
 				initialPose);
 
 		final EnchantArticulated result = new EnchantArticulated();
 		result.setRoot(model);
 
 		// 初期ポーズ
 		logger.info("_initialPoseユニット数=" + initialPose.size());
 		tempPoseMap.put("_initialPose", initialPose);
 
 		// ポーズデータ
 		final BufferedReader in = new BufferedReader(new InputStreamReader(
 				new FileInputStream(l3cFile), "Shift_JIS"));
 		String line;
 		boolean start = false;
 		while ((line = in.readLine()) != null) {
 			line = line.trim();
 			if (line.equals("Pose:")) {
 				start = true;
 			} else if (start) {
 				try {
 					final Pose pose = PoseLineParser.parse(line);
 					logger.info("Pose" + pose.getName() + "ユニット数="
 							+ pose.getUnitPose().size());
 					tempPoseMap.put(pose.getName(), Lists.transform(
 							pose.getUnitPose(), poseTransformFunc));
 				} catch (Exception e) {
 					if (line.substring(0, 1).matches("[0-9]")) {
 						logger.error("ポーズデータのパースに失敗 [" + line + "]", e);
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
 
 	private EnchantPoseUnit buildPoseTree(EnchantUnit unit,
 			Iterator<double[]> pIte) {
 		final EnchantPoseUnit node = new EnchantPoseUnit();
 		final double[] pose = pIte.next();
 		node.setPose(pose);
 
 		Quat4d q = createQuat(pose[0], pose[1], pose[2]);
 		node.setQuat(new double[] { q.x, q.y, q.z, q.w });
 
 		for (EnchantUnit child : unit.getChildUnits()) {
 			if (!pIte.hasNext()) {
 				break;
 			}
 			node.getChildUnits().add(buildPoseTree(child, pIte));
 		}
 
 		return node;
 	}
 
	private Quat4d createQuat(double rotX, double rotY, double rotZ) {
		Quat4d q = newQuat(0, 0, 0, 0);
		q.mul(newQuat(0, 1, 0, rotY));
		q.mul(newQuat(1, 0, 0, rotX));
		q.mul(newQuat(0, 0, 1, rotZ));
		q.normalize();
		return q;
	}

 	private Quat4d newQuat(double x, double y, double z, double rad) {
 		double l = Math.sqrt(x * x + y * y + z * z);
 		if (l != 0.0 && l != -0.0 && !Double.isNaN(l) && !Double.isInfinite(l)) {
 			x /= l;
 			y /= l;
 			z /= l;
 		}
 		double s = Math.sin(rad / 2);
 		double c = Math.cos(rad / 2);
		return new Quat4d(x * s, y * s, z * s, c);
 	}
 
 	private EnchantUnit convert(Unit unit, L3pConverter l3pConverter,
 			File baseDir, List<double[]> initailPose) throws Exception {
 		final EnchantUnit result = new EnchantUnit();
 
 		final List<EnchantMesh> l3p;
 		final File file = new File(baseDir, l3pFileName(unit));
 		if (!file.exists()) {
 			l3p = Lists.newArrayList();
 			logger.warn("L3Pファイルがない！ " + l3pFileName(unit) + ", "
 					+ file.getAbsolutePath());
 		} else {
 			final Matrix4d xform = Utils.getIdentity();
 			xform.mul(scale(unit));
 			xform.mul(mov(unit));
 			l3p = l3pConverter.convert(file, xform);
 		}
 
 		result.setL3p(l3p);
 		result.setBasePosition(basePosition(unit));
 		initailPose.add(initialPose(unit));
 
 		for (Unit child : unit.getChildUnits()) {
 			result.getChildUnits().add(
 					convert(child, l3pConverter, baseDir, initailPose));
 		}
 
 		return result;
 	}
 
 	private String l3pFileName(Unit unit) {
 		String result = unit.getL3pFileName().toLowerCase();
 		if (File.separatorChar != '\\') {
 			while (result.indexOf('\\') >= 0) {
 				result = result.replace('\\', File.separatorChar);
 			}
 		}
 		return result;
 	}
 
 	private Matrix4d scale(Unit unit) {
 		final Matrix4d result = Utils.getIdentity();
 		result.m00 = unit.getUnitScal()[1]; // x
 		result.m11 = unit.getUnitScal()[2]; // y
 		result.m22 = unit.getUnitScal()[0]; // z
 		return result;
 	}
 
 	private Matrix4d mov(Unit unit) {
 		final Matrix4d result = Utils.getIdentity();
 		final Vector3d v = new Vector3d();
 		v.x = unit.getUnitMov()[1] * SufConverter.C_RATE; // x
 		v.y = unit.getUnitMov()[2] * SufConverter.C_RATE; // y
 		v.z = unit.getUnitMov()[0] * SufConverter.C_RATE; // z
 		result.setTranslation(v);
 		return result;
 	}
 
 	private double[] basePosition(Unit unit) {
 		return new double[] {
 				// mov
 				unit.getMov()[1] * SufConverter.C_RATE, // x
 				unit.getMov()[2] * SufConverter.C_RATE, // y
 				unit.getMov()[0] * SufConverter.C_RATE, // z
 		};
 	}
 
 	private double[] initialPose(Unit unit) {
 		return new double[] {
 				// rot
 				unit.getRoty() * Math.PI / 180, // x
 				unit.getRotz() * Math.PI / 180, // y
 				unit.getRotx() * Math.PI / 180, // z
 
 				// mov
 				0, 0, 0 };
 	}
 
 	private Function<double[], double[]> poseTransformFunc = new Function<double[], double[]>() {
 		@Override
 		public double[] apply(double[] up) {
 			return new double[] {
 					// rot
 					up[4] * Math.PI / 180, // x
 					up[5] * Math.PI / 180, // y
 					up[3] * Math.PI / 180, // z
 
 					// mov
 					up[1] * SufConverter.C_RATE, // x
 					up[2] * SufConverter.C_RATE, // y
 					up[0] * SufConverter.C_RATE, // z
 			};
 		}
 	};
 
 }
