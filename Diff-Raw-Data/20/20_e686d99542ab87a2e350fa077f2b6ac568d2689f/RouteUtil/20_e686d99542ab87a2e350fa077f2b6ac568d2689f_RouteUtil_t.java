 package com.bastengao.struts2.freeroute;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.bastengao.struts2.freeroute.annotation.MethodType;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * 路由工具类
  *
  * @author bastengao
  * @date 12-12-18 23:25
  * @since 1.0
  */
 public class RouteUtil {
 
     //路径变量正则 "/{([a-zA-Z]+)}"
     public static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("/\\{([a-zA-Z]+)\\}");
 
     private RouteUtil() {
     }
 
     /**
      * 将路由解析为 action 信息
     *
     * 如果是静态路由 "/persons/new" 将变成 "/persons/new#{actionMethodName}@{actionClassName}"
     * 如果是动态路由 "/persons/{id}" 将变成 "/persons/__id__#{actionMethodName}@{actionClassName}"
      *
      * @param routeMapping
      * @return
      */
     public static ActionInfo routeToAction(RouteMapping routeMapping) {
        // TODO 优化：RouteMapping 可缓存 ActionInfo, 因为规则一致每次的结果都一样

         String routePath = routeMapping.getRoutePath();
         routePath = ActionUtil.padSlash(routePath);
         routePath = RouteUtil.flatRoutePath(routePath);
 
         String namespace = ActionUtil.namespace(routePath);
         String actionName = ActionUtil.actionName(routePath);

        String methodName = routeMapping.getMethod().getName();
        String className = routeMapping.getAction().getName();

        // TODO 优化：为了避免名字冲突，目前简单粗暴的使用 方法名 + 类名 的方式命名
        // 缺点就是 actionName 太长，可能会影响性能
        actionName = actionName + "#" + methodName + "@" + className;
         return new ActionInfo(namespace, actionName);
     }
 
     /**
      * 是否有 pathVariable
      *
      * @param routePath
      * @return
      */
     public static boolean hasPathVariables(String routePath) {
         Matcher matcher = PATH_VARIABLE_PATTERN.matcher(routePath);
         return matcher.find();
     }
 
     /**
      * 将路由路径转化为能够匹配此路径请求的正则表达式
      * <p/>
      *
      * @param routePath
      * @return
      */
     public static String toRoutePathPattern(String routePath) {
         //1. 数字，英文
         // "/([0-9a-zA-Z]+)"
 
         //2. 数字, 英文, 中文
         // 0-9, a-zA-Z, \u4e00-\u9fa5 => "/([0-9a-zA-Z\\\\u4e00-\\\\u9fa5]+)"
         // (为什么是 \\\\ ，因为在正则表达式里要表示 \ 需要转义为 \\,
         // 但在 java 里表示 \\, 也需要转义就变成了 \\\\)
 
         //3. 之前两种方案是白名单，可考虑黑名单方案. 排除掉保留字的都可匹配,
         // 关键字有:
         // ! * ' ( ) ; : @ & = + $ , / ? % # [ ]
         //参考 http://en.wikipedia.org/wiki/Uniform_resource_locator#List_of_allowed_URL_characters
         //
         // \p{Punct} =>
         // ! * ' ( ) ; : @ & = + $ , / ? % # [ ] - .  < > \ ^ _ ` { | } ~ "
         // "[\-\.<>\\\^_`\{\}\|~"[^\p{Punct}]]+"
 
         // 目前排除所有的标点符号的字符，但只保留了 "."  "_"  "~"  "-"
         return PATH_VARIABLE_PATTERN.matcher(routePath).replaceAll("/([\\._~\\-[^\\\\p{Punct}]]+)");
     }
 
     /**
      * 返回路由中的变量名
      *
      * @param routePath
      * @return
      */
     public static List<String> pathVariableNames(String routePath) {
         Matcher matcher = PATH_VARIABLE_PATTERN.matcher(routePath);
         List<String> variableNames = new ArrayList<String>();
         while (matcher.find()) {
             variableNames.add(matcher.group(1));
         }
 
         // 判断变量名不能相同
         Set<String> checkVariableNames = new HashSet<String>();
         for (String name : variableNames) {
             if (!checkVariableNames.add(name)) {
                 throw new IllegalArgumentException(String.format("same path variable name [%s] at [%s]", name, routePath));
             }
         }
         return variableNames;
     }
 
     /**
      * 如果 routePath 中有 pathVariable,
      * 例如 "/persons/{id}" 那么将路由转化为 "/persons/__id__"
      * "/persons/{id}/edit" 转化为 "/persons/__id__/edit"
      *
      * @param routePath
      * @return
      */
     @VisibleForTesting
     public static String flatRoutePath(String routePath) {
         StringBuilder flatRoutePath = new StringBuilder();
         //下一次匹配的开始
         int nextStart = 0;
         Matcher matcher = PATH_VARIABLE_PATTERN.matcher(routePath);
         while (matcher.find()) {
             String variableName = matcher.group(1);
             int matchStart = matcher.start(); //此次匹配的开始
             int matchEnd = matcher.end(); //引次匹配的结束
 
             flatRoutePath.append(routePath.substring(nextStart, matchStart));
             flatRoutePath.append("/__").append(variableName).append("__");
 
             nextStart = matchEnd;
         }
         flatRoutePath.append(routePath.substring(nextStart));
 
         return flatRoutePath.toString();
     }
 
     /**
      * 根据 http method 返回对应的枚举
      * 如果没有匹配的返回 MethodType.NONE
      *
      * @param method
      * @return
      */
     public static MethodType valueOfMethod(String method) {
         try {
             return MethodType.valueOf(method.toUpperCase());
         } catch (IllegalArgumentException e) {
             return MethodType.NONE;
         }
     }
 }
